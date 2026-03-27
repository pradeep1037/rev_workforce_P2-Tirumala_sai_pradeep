import os
import glob

def add_holidays_to_sidebars():
    template_dir = r"c:\Users\nihar\OneDrive\Desktop\revworkforce__2\src\main\resources\templates"
    search_str = '<a class="nav-link" th:href="@{/admin/leaves}"><span class="icon">📑</span>All Leaves</a>'
    active_search_str = '<a class="nav-link active" th:href="@{/admin/leaves}"><span class="icon">📑</span>All Leaves</a>'
    
    replace_str = '<a class="nav-link" th:href="@{/admin/leaves}"><span class="icon">📑</span>All Leaves</a>\n                <a class="nav-link" th:href="@{/admin/holidays}"><span class="icon">🌴</span>Holidays</a>'
    
    files = glob.glob(f"{template_dir}/**/*.html", recursive=True)
    count = 0
    for file in files:
        with open(file, 'r', encoding='utf-8') as f:
            content = f.read()

        updated = False
        if search_str in content:
            content = content.replace(search_str, replace_str)
            updated = True
            
        # specifically for admin/leaves.html where the active link also acts as insertion point
        # actually, active_search_str should also just get active leaves and regular holidays
        active_replace_str = '<a class="nav-link active" th:href="@{/admin/leaves}"><span class="icon">📑</span>All Leaves</a>\n                <a class="nav-link" th:href="@{/admin/holidays}"><span class="icon">🌴</span>Holidays</a>'
        if active_search_str in content:
            content = content.replace(active_search_str, active_replace_str)
            updated = True

        if updated:
            with open(file, 'w', encoding='utf-8') as f:
                f.write(content)
            count += 1
            print(f"Updated {file}")
            
    print(f"Total files updated: {count}")

if __name__ == "__main__":
    add_holidays_to_sidebars()
