from fastapi import FastAPI
import pandas as pd
import numpy as np
import joblib

app = FastAPI()

# ---------------- LOAD FILES ----------------
model = joblib.load("model.pkl")
market_baseline = joblib.load("market_baseline.pkl")
state_baseline = joblib.load("state_baseline.pkl")
grand_mean = joblib.load("grand_mean.pkl")

# load dataset for lag features
df = pd.read_csv("Agriculture_price_dataset.csv")
df['Price Date'] = pd.to_datetime(df['Price Date'])


# ---------------- PREPROCESS FUNCTION ----------------
def preprocess(s):
    return str(s).lower().strip().replace(' ', '_')


# ---------------- ROOT ----------------
@app.get("/")
def home():
    return {"message": "Mandi Price Prediction API is running 🚀"}


# ---------------- PREDICT ----------------
@app.post("/predict")
def predict(data: dict):
    try:
        # Convert input to DataFrame
        new_data = pd.DataFrame([data])

        # Date features
        new_data['Price Date'] = pd.to_datetime(new_data['Price Date'])
        new_data['Year'] = new_data['Price Date'].dt.year
        new_data['Month'] = new_data['Price Date'].dt.month

        # Preprocess
        for col in ['STATE','District Name','Market Name','Commodity','Variety','Grade']:
            new_data[col] = new_data[col].apply(preprocess)

        # Attach baselines
        new_data = new_data.join(market_baseline, on=['Market Name','Commodity'])
        new_data = new_data.join(state_baseline, on=['STATE','Commodity'])

        new_data[['Market_Commodity_Baseline','State_Commodity_Baseline']] = \
            new_data[['Market_Commodity_Baseline','State_Commodity_Baseline']].fillna(grand_mean)

        # ---------------- LAG FEATURES ----------------
        history = df[
            (df['Market Name'].str.lower().str.replace(' ','_') == new_data['Market Name'][0]) &
            (df['Commodity'].str.lower().str.replace(' ','_') == new_data['Commodity'][0])
        ].sort_values('Price Date')

        if len(history) < 30:
            return {"error": "Not enough historical data"}

        new_data['Lag_1']  = history['Modal_Price'].iloc[-1]
        new_data['Lag_3']  = history['Modal_Price'].iloc[-3]
        new_data['Lag_7']  = history['Modal_Price'].iloc[-7]
        new_data['Lag_14'] = history['Modal_Price'].iloc[-14]
        new_data['Lag_30'] = history['Modal_Price'].iloc[-30]

        # Rolling features
        new_data['Roll_mean_7']  = history['Modal_Price'].iloc[-7:].mean()
        new_data['Roll_std_7']   = history['Modal_Price'].iloc[-7:].std()

        new_data['Roll_mean_14'] = history['Modal_Price'].iloc[-14:].mean()
        new_data['Roll_std_14']  = history['Modal_Price'].iloc[-14:].std()

        new_data['Roll_mean_30'] = history['Modal_Price'].iloc[-30:].mean()
        new_data['Roll_std_30']  = history['Modal_Price'].iloc[-30:].std()

        # Drop date
        x_new = new_data.drop(columns=['Price Date'])

        # Prediction
        prediction = model.predict(x_new)[0]

        return {
            "predicted_price_per_100kg": round(prediction, 2)
        }

    except Exception as e:
        return {"error": str(e)}