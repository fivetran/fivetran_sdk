from dataclasses import dataclass
from typing import List, Dict, Any
from enum import Enum


class Distance(Enum):
    COSINE = 1
    DOT = 2
    EUCLIDIAN = 3


@dataclass
class Metrics:
    distance: Distance
    dimensions: int


@dataclass
class Collection:
    name: str
    metrics: Metrics


@dataclass
class Row:
    id: str
    vector: List[float]
    content: str
    payload: Dict[str, Any]


