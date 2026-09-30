@echo off
setlocal
set "SCRIPT_DIR=%~dp0"

echo ================================================================
echo MoteurEchecs - Suite complete de reproductibilite (une commande)
echo ================================================================
echo.

echo [1/6] Compilation...
call mvn -q -f "%SCRIPT_DIR%..\pom.xml" compile
if errorlevel 1 (
    echo.
    echo ECHEC : la compilation a echoue.
    exit /b 1
)
echo OK.
echo.

echo [2/6] Suite de tests (51 tests attendus, aucun echec tolere)...
call mvn -q -f "%SCRIPT_DIR%..\pom.xml" test
if errorlevel 1 (
    echo.
    echo ECHEC : au moins un test a echoue. Arret de la suite.
    exit /b 1
)
echo OK - tous les tests passent.
echo.

echo [3/6] Baseline Minimax naif vs Alpha-Beta (profondeurs 3 et 4)...
"%JAVA_HOME%\bin\java.exe" -cp "%SCRIPT_DIR%..\target\classes" com.moteurechecs.Main
echo.

echo [4/6] Localite memoire : grille d'objets vs bitboards (mesure isolee)...
"%JAVA_HOME%\bin\java.exe" -cp "%SCRIPT_DIR%..\target\classes" com.moteurechecs.experimentation.EtapeLocaliteMemoire
echo.

echo [5/6] Recherche optimisee (Alpha-Beta + zero-allocation + pre-allocation + tri des coups, profondeur 5)...
"%JAVA_HOME%\bin\java.exe" -cp "%SCRIPT_DIR%..\target\classes" com.moteurechecs.experimentation.DiagnosticAllocations
echo.

echo [6/6] Recherche a budget de temps (profondeur adaptative, 5 budgets)...
"%JAVA_HOME%\bin\java.exe" -cp "%SCRIPT_DIR%..\target\classes" com.moteurechecs.experimentation.DiagnosticBudgetTemps
echo.

echo ================================================================
echo Suite terminee sans erreur.
echo ================================================================
