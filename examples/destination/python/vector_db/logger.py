import json


def log(msg: str):
    m = {
        "level": "INFO",
        "message": msg,
        "message-origin": "sdk_destination"
    }
    print(json.dumps(m), flush=True)