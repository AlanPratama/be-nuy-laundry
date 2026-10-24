#!/usr/bin/env python3
"""
BE Nuy Laundry - High-Performance Database Seeder (PERF-00)
Generates high-volume dummy data directly via batch SQL for realistic performance testing.
"""

import os
import sys
import uuid
import random
import subprocess
from datetime import datetime, timedelta

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "nuy_laundry")
DB_USER = os.getenv("DB_USER", "postgres")

# Target volumes
NUM_USERS = 50
NUM_CUSTOMERS = 2000
NUM_BENEFITS = 20
NUM_PACKAGES = 30
NUM_TRANSACTIONS = 10000

BCRYPT_PASS = "$2a$10$LTc4VMymbH0YIlx2m.hOFeui4LO5jEDoUOUFstRvr8XWrj0AGpttS"  # Password123!

def run_psql(sql_content):
    env = os.environ.copy()
    env["PGPASSWORD"] = os.getenv("DB_PASSWORD", "postgres")
    proc = subprocess.Popen(
        ["psql", "-h", DB_HOST, "-p", DB_PORT, "-U", DB_USER, "-d", DB_NAME, "-v", "ON_ERROR_STOP=1"],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        env=env
    )
    stdout, stderr = proc.communicate(input=sql_content)
    if proc.returncode != 0:
        print(f"[ERROR] psql failed:\n{stderr}")
        sys.exit(1)
    return stdout

def main():
    print("=========================================================================")
    print("            BE NUY LAUNDRY - DATA SEEDER (PERF-00)                      ")
    print("=========================================================================")
    print(f"Connecting to {DB_USER}@{DB_HOST}:{DB_PORT}/{DB_NAME}...")

    now = datetime.now()
    now_str = now.strftime("%Y-%m-%d %H:%M:%S")

    sql_parts = ["BEGIN;\n"]

    print(f"[1/5] Generating {NUM_USERS} Users...")
    user_ids = []
    user_rows = []
    # Dedicated perf admin user
    perf_admin_id = str(uuid.uuid4())
    user_ids.append(perf_admin_id)
    user_rows.append(f"('{perf_admin_id}', 'perf_admin', '{BCRYPT_PASS}', 'Perf Admin User', '081299990000', 'Jl. Testing No. 1', 'ADMIN', true, '{now_str}', '{now_str}')")

    for i in range(1, NUM_USERS):
        uid = str(uuid.uuid4())
        user_ids.append(uid)
        role = "ADMIN" if i < 5 else "STAFF"
        user_rows.append(f"('{uid}', 'user_perf_{i}', '{BCRYPT_PASS}', 'Staff Operator {i}', '0812{i:08d}', 'Jl. Operasional No. {i}', '{role}', true, '{now_str}', '{now_str}')")

    sql_parts.append("INSERT INTO users (id, username, password, name, phone, address, role, is_active, created_at, updated_at) VALUES\n" + ",\n".join(user_rows) + " ON CONFLICT (username) DO NOTHING;\n")

    print(f"[2/5] Generating {NUM_CUSTOMERS} Customers...")
    customer_ids = []
    customer_rows = []
    first_names = ["Budi", "Siti", "Agus", "Dewi", "Rian", "Eka", "Putri", "Bayu", "Rina", "Doni", "Maya", "Fajar", "Lestari", "Hadi", "Indah"]
    last_names = ["Santoso", "Pratama", "Wijaya", "Kusuma", "Hidayat", "Lestari", "Nugroho", "Saputra", "Wibowo", "Siregar"]

    for i in range(NUM_CUSTOMERS):
        cid = str(uuid.uuid4())
        customer_ids.append(cid)
        cname = f"{random.choice(first_names)} {random.choice(last_names)} {i}"
        phone = f"0857{i:08d}"
        addr = f"Jl. Pelanggan Mawar No. {random.randint(1, 500)}, RT {random.randint(1, 10):02d}/RW {random.randint(1, 10):02d}"
        customer_rows.append(f"('{cid}', '{cname}', '{phone}', '{addr}', '{now_str}', '{now_str}')")

    sql_parts.append("INSERT INTO customers (id, name, phone, address, created_at, updated_at) VALUES\n" + ",\n".join(customer_rows) + ";\n")

    print(f"[3/5] Generating {NUM_BENEFITS} Benefits & {NUM_PACKAGES} Packages...")
    benefit_ids = []
    benefit_rows = []
    benefit_names = ["Parfum Premium", "Setrika Uap", "Anti Bakteri", "Kemasan Vakum", "Deterjen Hipoalergenik",
                     "Pemisahan Warna", "Antar Jemput Gratis", "Dry Clean Specialist", "Pewangi Lavender", "Pewangi Sakura",
                     "Cepat Kering", "Tanpa Kerut", "Cuci Khusus Wool", "Lipatan Rapi", "Pelindung Kancing",
                     "Kantong Pakaian Ramah Lingkungan", "Pengecekan Noda", "Garansi Cuci Ulang", "Express 6 Jam", "Pembersih Karpet"]
    for i, bname in enumerate(benefit_names):
        bid = str(uuid.uuid4())
        benefit_ids.append(bid)
        benefit_rows.append(f"('{bid}', '{bname} {i+1}', '{now_str}', '{now_str}')")
    sql_parts.append("INSERT INTO benefits (id, name, created_at, updated_at) VALUES\n" + ",\n".join(benefit_rows) + ";\n")

    package_ids = []
    package_rows = []
    pkg_types = ["WEIGHT", "PCS"]
    pkg_prefixes = ["Cuci Komplit Reguler", "Cuci Kering Lipat", "Cuci Kilat Express", "Dry Clean Jas", "Cuci Bedcover", "Cuci Sepatu", "Cuci Boneka", "Cuci Helm", "Cuci Karpet", "Cuci Gorden"]
    for i in range(NUM_PACKAGES):
        pid = str(uuid.uuid4())
        package_ids.append(pid)
        pname = f"{random.choice(pkg_prefixes)} Tier {i+1}"
        ptype = random.choice(pkg_types)
        price = random.choice([8000, 10000, 12000, 15000, 20000, 25000, 35000, 50000])
        days = random.randint(1, 4)
        package_rows.append(f"('{pid}', '{pname}', 'Layanan laundry berkualitas tinggi untuk kebutuhan harian', {days}, '{ptype}', {price}, '{now_str}', '{now_str}')")
    sql_parts.append("INSERT INTO packages (id, name, description, days_estimation, type, price, created_at, updated_at) VALUES\n" + ",\n".join(package_rows) + ";\n")

    # Package-Benefit mappings
    pb_rows = []
    for pid in package_ids:
        assigned_benefits = random.sample(benefit_ids, k=random.randint(1, 3))
        for bid in assigned_benefits:
            pbid = str(uuid.uuid4())
            pb_rows.append(f"('{pbid}', '{pid}', '{bid}', '{now_str}', '{now_str}')")
    sql_parts.append("INSERT INTO packages_benefits (id, package_id, benefit_id, created_at, updated_at) VALUES\n" + ",\n".join(pb_rows) + ";\n")

    print(f"[4/5] Generating {NUM_TRANSACTIONS} Transactions & Details...")
    trx_rows = []
    detail_rows = []
    statuses = ["IN_PROGRESS", "DONE", "CANCELED"]

    base_time = now - timedelta(days=60)

    for i in range(NUM_TRANSACTIONS):
        tid = str(uuid.uuid4())
        cid = random.choice(customer_ids)
        uid = random.choice(user_ids)
        invoice = f"INV-PERF-{i+1:06d}"
        status = random.choice(statuses)

        offset_days = random.randint(0, 60)
        order_dt = base_time + timedelta(days=offset_days, hours=random.randint(8, 17))
        target_dt = order_dt + timedelta(days=random.randint(1, 4))
        order_str = order_dt.strftime("%Y-%m-%d %H:%M:%S")
        target_str = target_dt.strftime("%Y-%m-%d %H:%M:%S")

        num_details = random.randint(1, 3)
        total_price = 0

        for d_idx in range(num_details):
            did = str(uuid.uuid4())
            pid = random.choice(package_ids)
            qty = random.randint(1, 5)
            item_price = random.choice([10000, 15000, 20000, 25000]) * qty
            total_price += item_price

            detail_rows.append(
                f"('{did}', '{tid}', '{pid}', 'Paket Laundry Detail', 'Deskripsi detail paket', 2, {qty}, {item_price}, 'KILO', '{target_str}', '{order_str}', '{order_str}')"
            )

        trx_rows.append(
            f"('{tid}', '{cid}', '{uid}', '{invoice}', {total_price}, '{order_str}', '{target_str}', '{status}', 'Customer {i}', '081299999999', 'Alamat Dummy', '{order_str}', '{order_str}')"
        )

    sql_parts.append("INSERT INTO transactions (id, customer_id, user_id, invoice_no, total_price, order_date, target_date, status, customer_name, customer_phone, customer_address, created_at, updated_at) VALUES\n" + ",\n".join(trx_rows) + ";\n")
    sql_parts.append("INSERT INTO transaction_details (id, transaction_id, package_id, package_name, package_description, package_days_estimation, quantity, total_price, package_type, target_date, created_at, updated_at) VALUES\n" + ",\n".join(detail_rows) + ";\n")

    sql_parts.append("COMMIT;\n")

    full_sql = "".join(sql_parts)
    print(f"[5/5] Executing SQL batch insert ({len(full_sql)/1024/1024:.2f} MB)...")
    run_psql(full_sql)

    print("\n[SUCCESS] Seeding completed successfully!")
    # Verification count
    verify_sql = """
    SELECT 'users' as tbl, count(*) FROM users
    UNION ALL SELECT 'customers', count(*) FROM customers
    UNION ALL SELECT 'packages', count(*) FROM packages
    UNION ALL SELECT 'benefits', count(*) FROM benefits
    UNION ALL SELECT 'transactions', count(*) FROM transactions
    UNION ALL SELECT 'transaction_details', count(*) FROM transaction_details;
    """
    counts = run_psql(verify_sql)
    print("\nCurrent Database Counts:")
    print(counts)

if __name__ == "__main__":
    main()
