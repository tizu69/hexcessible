import requests

url = "https://raw.githubusercontent.com/object-Object/HexBug/refs/heads/main/bot/src/HexBug/resources/numbers_2000.json"
data = requests.get(url).json()

numbers: dict[int, str] = {}
maxNumber = 0
for k, v in data.items():
    if int(k) > 0:
        maxNumber = max(maxNumber, int(k))
        numbers[int(k)] = v[1][4:]

with open("../src/main/resources/numbers.txt", "w") as f:
    for i in range(maxNumber + 1):
        if i in numbers:
            f.write(f"{numbers[i]}")
            if i < maxNumber:
                f.write("\n")
