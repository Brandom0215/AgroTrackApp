import os

files_to_update = [
    "app/src/main/java/pa/ac/utp/agrotrackapp/data/alertas/AlertManager.kt",
    "app/src/main/java/pa/ac/utp/agrotrackapp/ui/auth/LoginActivity.kt",
    "app/src/main/java/pa/ac/utp/agrotrackapp/ui/auth/PerfilUsuarioActivity.kt"
]

for file in files_to_update:
    with open(file, "r", encoding="utf-8") as f:
        content = f.read()
    
    content = content.replace("getSharedPreferences(\"GanaDEXAuthPrefs\", Context.MODE_PRIVATE)", "pa.ac.utp.agrotrackapp.data.auth.AuthPrefsHelper.getAuthPrefs(this)")
    content = content.replace("getSharedPreferences(\"GanaDEXAuthPrefs\", MODE_PRIVATE)", "pa.ac.utp.agrotrackapp.data.auth.AuthPrefsHelper.getAuthPrefs(this)")
    content = content.replace("context.getSharedPreferences(\"GanaDEXAuthPrefs\", Context.MODE_PRIVATE)", "pa.ac.utp.agrotrackapp.data.auth.AuthPrefsHelper.getAuthPrefs(context)")
    
    with open(file, "w", encoding="utf-8") as f:
        f.write(content)
print("Updated all GanaDEXAuthPrefs references")
