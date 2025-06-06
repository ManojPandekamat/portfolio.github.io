import mysql.connector
from datetime import datetime
class FenwickTree:
    def __init__(self, size):
        self.tree = [0] * (size + 1)

    def update(self, index, value):
        while index < len(self.tree):
            self.tree[index] += value
            index += index & -index

    def query(self, index):
        res = 0
        while index > 0:
            res += self.tree[index]
            index -= index & -index
        return res

conn = mysql.connector.connect(
    host="your-host",
    user="your-username",
    password="your-password",
    database="your-database"
)
cursor = conn.cursor()

cursor.execute("SELECT order_date, amount FROM orders ORDER BY order_date")
rows = cursor.fetchall()

# Create date to index mapping
date_list = sorted(list({row[0] for row in rows}))
date_to_index = {date: i+1 for i, date in enumerate(date_list)}  # 1-based index

ft = FenwickTree(len(date_list))
for date, amount in rows:
    idx = date_to_index[date]
    ft.update(idx, float(amount))

cursor.execute("DROP TABLE IF EXISTS daily_revenue_prefix")
cursor.execute("""
CREATE TABLE daily_revenue_prefix (
    revenue_date DATE PRIMARY KEY,
    prefix_sum DECIMAL(10,2)
)
""")

for date, idx in date_to_index.items():
    psum = ft.query(idx)
    cursor.execute("INSERT INTO daily_revenue_prefix (revenue_date, prefix_sum) VALUES (%s, %s)", (date, psum))

conn.commit()

def get_revenue(start_date: str, end_date: str):
    # Get prefix sum for end and (start - 1)
    cursor.execute("SELECT prefix_sum FROM daily_revenue_prefix WHERE revenue_date <= %s ORDER BY revenue_date DESC LIMIT 1", (end_date,))
    end_val = cursor.fetchone()
    cursor.execute("SELECT prefix_sum FROM daily_revenue_prefix WHERE revenue_date < %s ORDER BY revenue_date DESC LIMIT 1", (start_date,))
    start_val = cursor.fetchone()
    return round((end_val[0] if end_val else 0) - (start_val[0] if start_val else 0), 2)

print("Revenue from 2024-01-01 to 2024-03-01:", get_revenue("2024-01-01", "2024-03-01"))

cursor.close()
conn.close()
