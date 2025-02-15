import pandas as pd
import requests

def load_csv(file_name, sep=";"):
    """
    Carga un CSV usando el separador indicado y convierte la columna 'valor'
    a número (creando 'valor_num').
    """
    try:
        df = pd.read_csv(file_name, sep=sep, on_bad_lines='skip')
        df["valor_num"] = df["valor"].str.replace(",", ".")
        df["valor_num"] = pd.to_numeric(df["valor_num"], errors="coerce")
        return df
    except Exception as e:
        print(f"Error al cargar {file_name}: {e}")
        return None

def pivot_dataframe(df):
    """
    Pivota el DataFrame para que cada municipio quede en una fila y cada concepto en una columna.
    Se utiliza la media para agrupar en caso de duplicados.
    """
    try:
        pivot = df.pivot_table(index="municipi", columns="concepte", values="valor_num", aggfunc="mean")
        pivot.reset_index(inplace=True)
        return pivot
    except Exception as e:
        print("Error al pivotear el DataFrame:", e)
        return None

def load_api_data(url):
    """
    Obtiene datos desde la API y devuelve un DataFrame.
    """
    try:
        response = requests.get(url)
        response.raise_for_status()
        data = response.json()
        df_api = pd.DataFrame(data)
        return df_api
    except Exception as e:
        print(f"Error al obtener datos de la API {url}: {e}")
        return None

def main():
    # --- Cargar y procesar los CSVs ---
    df_censph = load_csv("censph16400mun.csv", sep=";")
    df_ist1   = load_csv("ist14034mun.csv", sep=";")
    df_ist2   = load_csv("ist14074mun.csv", sep=";")
    
    pivot_censph = pivot_dataframe(df_censph) if df_censph is not None else None
    pivot_ist1   = pivot_dataframe(df_ist1)   if df_ist1 is not None else None
    pivot_ist2   = pivot_dataframe(df_ist2)   if df_ist2 is not None else None
    
    print("\n--- Pivot CENSPH ---")
    if pivot_censph is not None:
        print(pivot_censph.head())
    
    print("\n--- Pivot IST 14034 ---")
    if pivot_ist1 is not None:
        print(pivot_ist1.head())
    
    print("\n--- Pivot IST 14074 ---")
    if pivot_ist2 is not None:
        print(pivot_ist2.head())
    
    # Fusionar los DataFrames pivoteados por "municipi"
    merged = None
    if pivot_censph is not None:
        merged = pivot_censph
    if pivot_ist1 is not None:
        merged = pd.merge(merged, pivot_ist1, on="municipi", how="outer", suffixes=("", "_ist1")) if merged is not None else pivot_ist1
    if pivot_ist2 is not None:
        merged = pd.merge(merged, pivot_ist2, on="municipi", how="outer", suffixes=("", "_ist2")) if merged is not None else pivot_ist2
    
    if merged is None or merged.empty:
        print("No se pudieron combinar los datos de los CSVs.")
        return
    
    # Lista de municipios del Tarragonès a conservar
    municipios_tarragones = [
        "altafulla", "la canonja", "el catllar", "constantí", "creixell",
        "el morell", "la nou de gaià", "els pallaresos", "perafort",
        "la pobla de mafumet", "la pobla de montornès", "renau",
        "la riera de gaià", "roda de berà", "salomó", "salou",
        "la secuita", "tarragona", "torredembarra", "vespella de gaià",
        "vila-seca", "vilallonga del camp"
    ]
    
    merged["municipi_lower"] = merged["municipi"].str.lower()
    filtered = merged[merged["municipi_lower"].isin(municipios_tarragones)].drop(columns=["municipi_lower"])
    
    print("\n--- Datos combinados filtrados para municipios del Tarragonès ---")
    print(filtered.head(10))
    
    # --- Cargar datos adicionales de la API ---
    api_url = "https://analisi.transparenciacatalunya.cat/resource/rcqh-tgqn.json"
    df_api = load_api_data(api_url)
    if df_api is None or df_api.empty:
        print("No se pudieron obtener datos de la API.")
        return
    
    print("\n--- Datos de la API rcqh-tgqn.json (sin filtrar) ---")
    print(df_api.head(10))
    
    # Filtrar por año 2023: convertir la columna 'any' y filtrar
    if "any" in df_api.columns:
        df_api["any"] = pd.to_numeric(df_api["any"], errors="coerce")
        df_api = df_api[df_api["any"] == 2023]
    else:
        print("La columna 'any' no se encontró en los datos de la API.")
    
    # Eliminar la columna 'any'
    if "any" in df_api.columns:
        df_api = df_api.drop(columns=["any"])
    
    # Convertir 'municipi' a minúsculas y filtrar por los municipios del Tarragonès
    if "municipi" not in df_api.columns:
        print("La columna 'municipi' no se encontró en los datos de la API.")
        return
    df_api["municipi_lower"] = df_api["municipi"].str.lower()
    df_api_filtrado = df_api[df_api["municipi_lower"].isin(municipios_tarragones)].drop(columns=["municipi_lower"])
    
    print("\n--- Datos de la API filtrados para municipios del Tarragonès (año 2023) ---")
    print(df_api_filtrado.head(10))
    
    # Unir los datos de la API con los datos combinados de los CSVs (merge por "municipi")
    final_df = pd.merge(filtered, df_api_filtrado, on="municipi", how="outer")
    
    # Ahora, para la parte de discapacidad, solo queremos conservar la columna "total_discapacitats".
    # Asumiremos que en final_df aparecen columnas que contienen datos de discapacidad, como:
    # "total_del_33_al_64", "total_del_65_al_74", "homes_75_i_m_s", "dones_75_i_m_s", "total_75_i_m_s", "total_homes_discapacitats", "total_dones_discapacitades", "total_discapacitats"
    # De estas, conservaremos solo "total_discapacitats", y se eliminarán las demás.
    cols_to_drop = [col for col in final_df.columns if any(x in col for x in [
        "total_del_33_al_64", "total_del_65_al_74", "homes_75_i_m_s", 
        "dones_75_i_m_s", "total_75_i_m_s", "total_homes_discapacitats", 
        "total_dones_discapacitades", "codi", "homes_del_33_al_64", "dones_del_33_al_64", "homes_del_65_al_74", "dones_del_65_al_74"
    ])]
    final_df = final_df.drop(columns=cols_to_drop)
    # Opcional: renombrar "total_discapacitats" a "discapacitats_total" para mayor claridad.
    if "total_discapacitats" in final_df.columns:
        final_df = final_df.rename(columns={"total_discapacitats": "discapacitats_total"})
    
    print("\n--- Datos finales combinados y refinados ---")
    print(final_df.head(10))
    
    # Exportar el DataFrame final a CSV
    output_file = "datos_combinados_tarragones.csv"
    try:
        final_df.to_csv(output_file, index=False, sep=";")
        print(f"\nCSV final exportado correctamente a: {output_file}")
    except Exception as e:
        print("Error al exportar a CSV:", e)

if __name__ == "__main__":
    main()
