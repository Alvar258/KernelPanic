#!/usr/bin/env python3
import json
import re
import requests
import pandas as pd
from flask import Flask, request, jsonify

app = Flask(__name__)

# ------------------------------
# Funciones de Configuración y Limpieza
# ------------------------------
def load_config(config_file='config.json'):
    """Carga la configuración desde un archivo JSON."""
    try:
        with open(config_file, 'r') as f:
            config = json.load(f)
        return config
    except Exception as e:
        print(f"Error cargando el archivo de configuración: {e}")
        return {}

def clean_json_output(text):
    """Extrae y limpia el contenido JSON de una cadena formateada (por ejemplo, de bloques de código Markdown)."""
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
   asigna un "category" entre Infraestructura o Vial,
   y asigna un "priority_score" del 0 al 100 basándote en lo siguiente:

   **A. Gravedad del problema (0-40 puntos):**
      - 0-5: Problema casi inexistente o muy leve.
      - 6-20: Daño leve a moderado.
      - 21-40: Daño importante.

   **B. Afectación a la población vulnerable (0-30 puntos):**
      - 0-5: Poca o ninguna población vulnerable.
      - 6-25: Moderada.
      - 26-30: Alta.

   **C. Impacto en la seguridad y bienestar (0-30 puntos):**
      - 0-5: Sin impacto notable.
      - 6-20: Impacto moderado.
      - 21-30: Impacto alto.

La suma de estos tres criterios dará el "priority_score" total (entre 0 y 100).

Reporte de incidencia:
{incidencia}

Contexto:
{context}
"""
    payload = {
        "contents": [{
            "parts": [{
                "text": prompt
            }]
        }]
    }
    headers = {"Content-Type": "application/json"}
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
# Ruta API
# ------------------------------
@app.route("/clasificar", methods=["POST"])
def clasificar():
    data = request.get_json(force=True)
    if not data:
        return jsonify({"error": "El cuerpo de la solicitud está vacío."}), 400

    municipio = data.get("municipi")
    incidencia = data.get("incidencia")
    if not municipio or not incidencia:
        return jsonify({"error": "Faltan los parámetros 'municipi' o 'incidencia'."}), 400

    contexto = get_context_for_municipio(municipio)
    if contexto.startswith("Error") or contexto.startswith("No se encontraron"):
        return jsonify({"error": contexto}), 400

    resultado = classify_incident_with_context(incidencia, contexto)
    if resultado:
        return jsonify(resultado)
    else:
        return jsonify({"error": "No se pudo obtener una respuesta válida de la IA."}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)