import json
import re
import requests
import pandas as pd
import argparse

# ------------------------------
# Funciones de Configuración y Limpieza
# ------------------------------
def load_config(config_file='config.json'):
    """
    Carga la configuración desde un archivo JSON.
    """
    try:
        with open(config_file, 'r') as f:
            config = json.load(f)
        return config
    except Exception as e:
        print(f"Error cargando el archivo de configuración: {e}")
        return {}

def clean_json_output(text):
    """
    Extrae y limpia el contenido JSON de una cadena que puede estar formateada
    como bloque de código Markdown (por ejemplo, con ```json ... ```).
    """
    match = re.search(r"```(?:json)?\s*(\{.*\})\s*```", text, re.DOTALL)
    if match:
        return match.group(1)
    return text.strip()

# ------------------------------
# Función para obtener contexto desde el CSV combinado
# ------------------------------
def get_context_for_municipio(municipio, csv_file="datos_combinados_tarragones.csv"):
    """
    Carga el CSV con los datos combinados y filtra la información para el municipio dado.
    Devuelve un resumen en texto con los indicadores.
    """
    try:
        df = pd.read_csv(csv_file, sep=";")
        df["municipi_lower"] = df["municipi"].str.lower()
        municipio_lower = municipio.lower()
        df_filtro = df[df["municipi_lower"] == municipio_lower]
        if df_filtro.empty:
            return f"No se encontraron datos para el municipio: {municipio}"
        
        context_lines = [f"Datos para el municipio: {municipio}:"]
        # Se listan todas las columnas excepto 'municipi' y 'municipi_lower'
        for col in df_filtro.columns:
            if col not in ["municipi", "municipi_lower"]:
                valor = df_filtro.iloc[0][col]
                context_lines.append(f"- {col}: {valor}")
        return "\n".join(context_lines)
    except Exception as e:
        return f"Error al obtener contexto: {e}"

# ------------------------------
# Función de Clasificación de Incidencias (GeminiAI)
# ------------------------------
def classify_incident_with_context(incidencia, context):
    config = load_config()
    api_key = config.get('GEMINI_API_KEY')
    if not api_key:
        raise ValueError("No se encontró GEMINI_API_KEY en el archivo de configuración.")
    
    endpoint = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={api_key}"
    
    prompt = f"""Eres un analista de incidencias para la ciudad de Tarragona y especialista en análisis de riesgos urbanos. Tienes acceso a un bloque de contexto que contiene indicadores demográficos, socioeconómicos y de infraestructura específicos del municipio. Estos indicadores incluyen, entre otros:
- Edad mediana y mitjana.
- Proporción de población de 0-15, 16-64 y 65+.
- Índex socioeconòmic territorial (IST).
- Porcentaje de población ocupada.
- Total de discapacitats.

Debes usar estos datos para ajustar la evaluación del impacto de la incidencia.

Analiza el siguiente reporte de incidencia y, basándote en el contexto proporcionado, devuelve un JSON EXACTO con la siguiente estructura:

{{
  "is_spam": <true/false>,
  "category": "<categoria>",
  "priority_score": <número entero entre 0 y 100>
}}

Instrucciones:
1. Si el reporte contiene contenido publicitario, enlaces sospechosos o es irrelevante para incidencias reales en Tarragona, responde:
   "is_spam": true, "category": "", "priority_score": 0.
2. Si el reporte describe un problema real en la infraestructura, responde:
   "is_spam": false,
   "category": "infraestructura", y asigna un "priority_score" del 0 al 100 basándote en lo siguiente:

   **A. Gravedad del problema (0-40 puntos):**
   - 0-5: Problema casi inexistente o muy leve (daño imperceptible).
   - 6-20: Daño leve a moderado (grietas superficiales, deterioro estético).
   - 21-40: Daño importante (ej. adoquines sueltos, baches profundos con riesgo inmediato).

   **B. Afectación a la población vulnerable (0-30 puntos):**
   - 0-5: Poca o ninguna población vulnerable (porcentaje de 65+ muy bajo, por ejemplo <15%).
   - 6-25: Moderada (por ejemplo, entre 15% y 20% de población de 65+).
   - 26-30: Alta (por ejemplo, >20% de población de 65+ o presencia significativa de discapacitats).

   **C. Impacto en la seguridad y bienestar (0-30 puntos):**
   - 0-5: Sin impacto notable en la seguridad o movilidad.
   - 6-20: Impacto moderado (afecta a algunos, pero con recursos de respuesta adecuados).
   - 21-30: Impacto alto (riesgo de caídas, accidentes graves, comprometiendo la seguridad pública).

   **Importante:** Debes integrar de forma determinante los siguientes datos contextuales en tu evaluación:
   {context}

   Por ejemplo, si el municipio presenta un 22% de población de 65+ y un IST inferior a 95, la incidencia debe recibir una puntuación significativamente mayor que si dichos indicadores son bajos.

Finalmente, la suma de estos tres criterios dará el "priority_score" total (0 a 100). Asegúrate de que el puntaje refleje de forma proporcional la vulnerabilidad del contexto.

Reporte de incidencia:
{incidencia}

"""


    payload = {
        "contents": [{
            "parts": [{
                "text": prompt
            }]
        }]
    }
    headers = {
        "Content-Type": "application/json"
    }
    try:
        response = requests.post(endpoint, headers=headers, json=payload)
        print("Status Code:", response.status_code)
        print("Response Body:", response.text)
        response.raise_for_status()
    except Exception as e:
        print("Error al llamar a GeminiAI:", e)
        return None
    try:
        data = response.json()
        candidate = data.get("candidates", [])[0]
        raw_output = candidate.get("content", {}).get("parts", [{}])[0].get("text", "").strip()
    except Exception as e:
        print("Error al procesar la respuesta de GeminiAI:", e)
        return None
    cleaned_output = clean_json_output(raw_output)
    try:
        result = json.loads(cleaned_output)
    except Exception as e:
        print("Error al parsear JSON de la respuesta:", e)
        print("Respuesta obtenida:", cleaned_output)
        return None
    return result

# ------------------------------
# Función Principal usando argumentos
# ------------------------------
def main():
    parser = argparse.ArgumentParser(description="Clasifica incidencias en base a datos regionales.")
    parser.add_argument("--municipi", type=str, required=True, help="Nombre del municipio")
    parser.add_argument("--incidencia", type=str, required=True, help="Mensaje de la incidencia")
    args = parser.parse_args()
    
    municipio = args.municipi
    incidencia = args.incidencia
    
    # Obtener contexto del municipio desde el CSV de datos combinados
    contexto = get_context_for_municipio(municipio)
    print("\nContexto recopilado:")
    print(contexto)
    
    # Llamar a la función de clasificación pasando el contexto y el reporte
    resultado = classify_incident_with_context(incidencia, contexto)
    if resultado:
        print("\nResultado de la clasificación:")
        print(json.dumps(resultado, indent=2, ensure_ascii=False))
    else:
        print("\nNo se pudo obtener una respuesta válida de la IA.")

if __name__ == "__main__":
    main()
