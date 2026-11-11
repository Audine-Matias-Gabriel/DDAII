# GitHub Flow — Equipo Web de Ropa

Resumen de la guía de trabajo (`Guía de GitHub para el equipo — Web de Ropa.docx`).

## Cómo funciona

- `main` es siempre la versión que funciona. Nadie trabaja ni pushea directamente sobre `main`.
- Todo cambio entra por una rama corta y un Pull Request revisado por otro integrante.
- **Issue:** una tarjeta con una tarea (responsable, etiqueta P1..P5, hito).
- **Rama:** copia de `main` donde trabajás la tarea sin romper nada. Se llama `p<tu rol>/<tarea-corta>`, ej. `p2/soap-talles`.
- **Pull Request (PR):** el pedido de "sumá mis cambios a main". Lo revisa tu revisor fijo y los tests corren solos (CI).
- **Tablero (GitHub Projects):** Todo → En curso → En revisión → Hecho.

## Ciclo de una tarea (issue → main)

1. Crear el issue en GitHub (título `[P?] <tarea>`, responsable = vos, etiqueta = tu P, milestone = el hito).
2. Mover la tarjeta a **In Progress**.
3. Crear la rama desde `main` actualizada (`git checkout main && git pull`, `git checkout -b p<rol>/<tarea-corta>`).
4. Trabajar y commitear seguido (prefijos `feat:`, `fix:`, `test:`, `docs:`).
5. Publicar la rama (`git push -u origin <rama>`) y abrir el PR con `Closes #<número de issue>`.
6. Mover la tarjeta a **In Review**.
7. Esperar CI en verde y la aprobación del revisor fijo. Si pide cambios, commitear y pushear en la misma rama; el PR se actualiza solo.
8. **Squash and merge** (web). El issue se cierra solo, la tarjeta pasa a **Hecho**, la rama se borra.
9. Limpiar tu máquina: `git checkout main && git pull && git branch -d <rama>`.

## Revisores fijos

- P1 ↔ P4 (se revisan entre sí)
- P2 revisa a P3
- P3 revisa a P5
- P5 revisa a P2

## Reglas cortas

- Una tarea = un issue = una rama = un PR.
- Una rama vive 1 o 2 días como máximo. Si una tarea lleva más de un día, partirla en dos issues.
- Mantener tu rama al día y resolver conflictos cada mañana y antes de abrir el PR.
- Commits chicos con prefijo.
- Nunca `git push --force` sobre `main`.
- Nunca subir claves: van en `.env` (ignorado por git). En el repo solo queda `.env.example`.
- Si algo te bloquea más de 2 horas, avisar en el grupo en ese momento.

## Configuración del repositorio (día 1, dueño del repo)

1. Merge de `prototype` a `main` (ya hecho).
2. Settings → General → Pull Requests: solo **Allow squash merging**; destildar merge commits y rebase merging; tildar **Automatically delete head branches**.
3. Issues → Labels: crear P1, P2, P3, P4 y P5 (cada una de un color).
4. Issues → Milestones: crear Hito 1, Hito 2 y Hito 3 (fin día 3, 6 y 8 del plan).
5. Settings → Branches → Add branch protection rule (`main`): Require a pull request before merging (1 aprobación), Require status checks to pass (`backend`, `frontend`), Do not allow bypassing.
6. Projects → New project → Board "Web de Ropa", agregar columna **In Review** entre In Progress y Done. Agregar a los 5 integrantes con rol Write. Activar Auto-add to project.
7. En el repo: Projects → Link a project → elegir "Web de Ropa".

## Comandos de todos los días

```
# Al empezar el día
git checkout main && git pull
git checkout -b p<rol>/<tarea-corta>

# Mientras trabajás
git add .
git commit -m "feat: <descripcion>"
git push -u origin p<rol>/<tarea-corta>

# Después, abrir el PR en GitHub

# Si main avanzó mientras trabajabas
git checkout main && git pull
git checkout p<rol>/<tarea-corta>
git merge main
```

## Resolver conflictos

Si aparecen archivos en conflicto (`<<<<<<< HEAD`, `=======`, `>>>>>>> main`):
1. Abrir en el editor (VS Code / IntelliJ).
2. Dejar el texto como tiene que quedar; borrar las tres líneas de marcas.
3. Guardar. En GitHub Desktop el archivo pasa a **resolved**.
4. Compilar y correr los tests.
5. Tocar **Continue merge** y después **Push origin**.
6. Si no sabés qué quedarte, tocar **Abort merge**, avisar en el grupo y resolverlo con quien hizo el otro cambio.
