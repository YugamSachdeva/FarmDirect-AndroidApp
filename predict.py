import joblib
import pandas as pd

# load saved components
model = joblib.load("model.pkl")
market_baseline = joblib.load("market_baseline.pkl")
state_baseline = joblib.load("state_baseline.pkl")
grand_mean = joblib.load("grand_mean.pkl")

def preprocess(s):
    return str(s).lower().strip().replace(' ','_')

def predict_price(input_data, df):

    new_data = pd.DataFrame([input_data])

    # date features
    new_data['Price Date'] = pd.to_datetime(new_data['Price Date'])
    new_data['Year'] = new_data['Price Date'].dt.year
    new_data['Month'] = new_data['Price Date'].dt.month

    # preprocess categorical
    for col in ['STATE','District Name','Market Name','Commodity','Variety','Grade']:
        new_data[col] = new_data[col].apply(preprocess)

    # baselines
    new_data = new_data.join(market_baseline, on=['Market Name','Commodity'])
    new_data = new_data.join(state_baseline, on=['STATE','Commodity'])

    new_data[['Market_Commodity_Baseline','State_Commodity_Baseline']] = \
        new_data[['Market_Commodity_Baseline','State_Commodity_Baseline']].fillna(grand_mean)

    # get history
    history = df[
        (df['Market Name']==new_data['Market Name'][0]) &
        (df['Commodity']==new_data['Commodity'][0])
    ].sort_values('Price Date')

    # fallback if no history
    if len(history) < 30:
        for lag in [1,3,7,14,30]:
            new_data[f'Lag_{lag}'] = new_data['Market_Commodity_Baseline']

        for window in [7,14,30]:
            new_data[f'Roll_mean_{window}'] = new_data['Market_Commodity_Baseline']
            new_data[f'Roll_std_{window}'] = 0

    else:
        # lag features
        new_data['Lag_1']  = history['Modal_Price'].iloc[-1]
        new_data['Lag_3']  = history['Modal_Price'].iloc[-3]
        new_data['Lag_7']  = history['Modal_Price'].iloc[-7]
        new_data['Lag_14'] = history['Modal_Price'].iloc[-14]
        new_data['Lag_30'] = history['Modal_Price'].iloc[-30]

        # rolling features
        new_data['Roll_mean_7']  = history['Modal_Price'].iloc[-7:].mean()
        new_data['Roll_std_7']   = history['Modal_Price'].iloc[-7:].std()

        new_data['Roll_mean_14'] = history['Modal_Price'].iloc[-14:].mean()
        new_data['Roll_std_14']  = history['Modal_Price'].iloc[-14:].std()

        new_data['Roll_mean_30'] = history['Modal_Price'].iloc[-30:].mean()
        new_data['Roll_std_30']  = history['Modal_Price'].iloc[-30:].std()

    # final input
    x_new = new_data.drop(columns=['Price Date'])

    return model.predict(x_new)[0]