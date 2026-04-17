from functools import lru_cache

import joblib
from fastapi import FastAPI
from pydantic import BaseModel, Field

try:
    from .model_utils import MODEL_PATH, build_sample_frame
except ImportError:
    from model_utils import MODEL_PATH, build_sample_frame


class PricePredictionRequest(BaseModel):
    product_name: str = Field(..., examples=["Tomato"])
    category: str = Field(..., examples=["Vegetable"])
    season: str = Field(..., examples=["Summer"])
    location: str = Field(..., examples=["Greater Noida"])
    quantity_kg: float = Field(..., gt=0)
    is_organic: int = Field(..., ge=0, le=1)
    demand_index: float = Field(..., ge=0)


class PricePredictionResponse(BaseModel):
    predicted_price: float
    currency: str
    unit: str


@lru_cache(maxsize=1)
def load_model():
    if not MODEL_PATH.exists():
        raise FileNotFoundError(
            f"Model file not found at {MODEL_PATH}. Run train_price_model.py first."
        )
    return joblib.load(MODEL_PATH)


app = FastAPI(
    title="FarmDirect Price Prediction API",
    version="1.0.0",
    description="Simple API for price prediction in the FarmDirect app.",
)


@app.get("/health")
def health_check() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/predict-price", response_model=PricePredictionResponse)
def predict_price(payload: PricePredictionRequest) -> PricePredictionResponse:
    model = load_model()
    sample = build_sample_frame(
        product_name=payload.product_name,
        category=payload.category,
        season=payload.season,
        location=payload.location,
        quantity_kg=payload.quantity_kg,
        is_organic=payload.is_organic,
        demand_index=payload.demand_index,
    )
    predicted_price = float(model.predict(sample)[0])
    return PricePredictionResponse(
        predicted_price=round(predicted_price, 2),
        currency="INR",
        unit="per kg",
    )
