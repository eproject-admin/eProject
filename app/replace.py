import re

file_path = "/app/src/main/java/com/example/ui/screens/DashboardScreen.kt"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Replace contract division in rows
target_c_div = 'Text(c.division.take(12) + "...", modifier = Modifier.width(100.dp))'
replacement_c_div = 'Text(c.division.substringBefore(":").trim(), modifier = Modifier.width(100.dp))'
content = content.replace(target_c_div, replacement_c_div)

# 2. Replace opname division in rows
target_op_div = 'Text(op.division.take(12) + "...", modifier = Modifier.width(100.dp))'
replacement_op_div = 'Text(op.division.substringBefore(":").trim(), modifier = Modifier.width(100.dp))'
content = content.replace(target_op_div, replacement_op_div)

# 3. Replace contract row item width
target_c_item = 'Text(c.itemCodeAndName, modifier = Modifier.width(180.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)'
replacement_c_item = 'Text(c.itemCodeAndName, modifier = Modifier.width(220.dp), softWrap = true)'
content = content.replace(target_c_item, replacement_c_item)

# 4. Replace opname header item width
# Let's locate the opname table header which is around lines 2100-2105
# No, Tanggal, Lokasi, Sisi, Ruas Jalan, Divisi, Item Pekerjaan, Satuan
# Let's use a targeted regex or split to avoid ambiguity if there are multiple occurrences.
# Let's check how many times `Text("Item Pekerjaan", fontWeight = FontWeight.Bold, modifier = Modifier.width(180.dp))` appears.
# We already changed the first one in the contract table, so the remaining one should be the opname table!
target_op_header = 'Text("Item Pekerjaan", fontWeight = FontWeight.Bold, modifier = Modifier.width(180.dp))'
replacement_op_header = 'Text("Item Pekerjaan", fontWeight = FontWeight.Bold, modifier = Modifier.width(220.dp))'
content = content.replace(target_op_header, replacement_op_header)

# 5. Replace opname row item width
target_op_item = 'Text(op.itemName, modifier = Modifier.width(180.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)'
replacement_op_item = 'Text(op.itemName, modifier = Modifier.width(220.dp), softWrap = true)'
content = content.replace(target_op_item, replacement_op_item)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Done replacement!")
