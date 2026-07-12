import os
import glob
import re

directory = r"C:\Users\Lenovo P16v\Documents\Repos\AgroTrackApp\AgroTrackApp\app\src\main\res\layout"
files = glob.glob(os.path.join(directory, "*.xml"))

appbar_pattern = re.compile(r'(<com\.google\.android\.material\.appbar\.AppBarLayout[^>]*?)(/?>)', re.DOTALL)

for file in files:
    with open(file, "r", encoding="utf-8") as f:
        content = f.read()

    if "com.google.android.material.appbar.AppBarLayout" in content:
        def inject_attrs(match):
            attrs = match.group(1)
            end = match.group(2)
            
            # Remove existing to prevent duplicates
            attrs = re.sub(r'\s+app:liftOnScroll="[^"]*"', '', attrs)
            attrs = re.sub(r'\s+style="[^"]*"', '', attrs)
            attrs = re.sub(r'\s+app:elevation="[^"]*"', '', attrs)
            
            new_attrs = ' app:liftOnScroll="false" style="@style/Widget.MaterialComponents.AppBarLayout.PrimarySurface" app:elevation="0dp"'
            
            return attrs + new_attrs + end
            
        new_content = appbar_pattern.sub(inject_attrs, content)
        
        if new_content != content:
            with open(file, "w", encoding="utf-8") as f:
                f.write(new_content)
            print(f"Updated {os.path.basename(file)}")

print("Done.")
