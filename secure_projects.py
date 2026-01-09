import os

def secure_npm_yarn_projects(root_dir="."):
    """
    Végigjárja a megadott könyvtárat, és mindenhol, ahol package.json-t talál,
    elhelyezi a biztonsági beállításokat tartalmazó .npmrc és .yarnrc fájlokat.
    """
    
    npm_content = "ignore-scripts=true\n"
    yarn_content = "ignore-scripts true\n"
    
    count = 0
    
    print(f"Keresés indítása itt: {os.path.abspath(root_dir)}...\n")

    for dirpath, dirnames, filenames in os.walk(root_dir):
        # Kihagyjuk a node_modules mappákat a keresésből a sebesség érdekében
        if 'node_modules' in dirnames:
            dirnames.remove('node_modules')
            
        if 'package.json' in filenames:
            try:
                # .npmrc létrehozása/felülírása
                npmrc_path = os.path.join(dirpath, '.npmrc')
                with open(npmrc_path, 'w') as f:
                    f.write(npm_content)
                
                # .yarnrc létrehozása/felülírása
                yarnrc_path = os.path.join(dirpath, '.yarnrc')
                with open(yarnrc_path, 'w') as f:
                    f.write(yarn_content)
                
                print(f"[OK] Biztosítva: {dirpath}")
                count += 1
                
            except Exception as e:
                print(f"[HIBA] Nem sikerült írni ebben a mappában: {dirpath}. Hiba: {e}")

    print(f"\nKész! Összesen {count} projekt lett módosítva.")

if __name__ == "__main__":
    # Az aktuális könyvtárból indul
    secure_npm_yarn_projects()
