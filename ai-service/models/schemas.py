from pydantic import BaseModel
from typing import List


class AnalyzeRequest(BaseModel):
    text: str


class Entity(BaseModel):
    text: str
    label: str
    start: int
    end: int


class Relationship(BaseModel):
    source: str
    relation: str
    target: str


class AnalyzeResponse(BaseModel):
    entities: List[Entity]
    relationships: List[Relationship]