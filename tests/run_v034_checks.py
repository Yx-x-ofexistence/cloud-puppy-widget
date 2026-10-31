"""Bounded instrumentation runs. A zero adb exit code alone is not a passing test."""
from pathlib import Path
import os, re, subprocess, sys

ROOT=Path(__file__).resolve().parents[1]
ADB=str(ROOT/'.tools/android-sdk/platform-tools/adb.exe')
BASE=[ADB,'-s',os.environ.get('WIDGET_ADB_SERIAL','127.0.0.1:16416')]
OUT=ROOT/'artifacts/v0.3.4'
checks=[
 ('backup-regression','BackupChecks',None),
 ('backup-ui-regression','BackupUiChecks',None),
 ('mini-regression','PolishChecks','mini'),
 ('geometry-border-regression','PolishChecks',None),
 ('opacity-clock-drag-regression','PolishChecks','previous'),
 ('font-icon-regression','PolishChecks','features'),
 ('editor-ui-regression','RefactorChecks','ui'),
 ('upgrade-debug','SnapshotChecks',None),
]
if '--release' in sys.argv:checks=[('compat-release','V034CompatChecks',None),('backup-release','BackupChecks',None),('upgrade-release','SnapshotChecks',None)]
if '--only' in sys.argv:
    requested=sys.argv[sys.argv.index('--only')+1]
    checks=[check for check in checks if check[0]==requested]
    if not checks:raise SystemExit('Unknown check '+requested)
for name,runner,phase in checks:
    print('Running '+name,flush=True)
    command=BASE+['shell','am','instrument','-w']+(['-e','phase',phase] if phase else [])+['cn.xi.cloudwidget.test/cn.xi.cloudwidget.'+runner]
    for attempt in range(2):
        try:
            result=subprocess.run(command,capture_output=True,timeout=100)
            output=(result.stdout+result.stderr).decode('utf-8','replace')
            break
        except subprocess.TimeoutExpired as error:
            (OUT/(name+'-timeout.txt')).write_bytes((error.stdout or b'')+(error.stderr or b''))
            subprocess.run(BASE+['shell','am','force-stop','cn.xi.cloudwidget'],check=True)
            if attempt:raise
            subprocess.run(BASE+['shell','am','start','-W','-n','cn.xi.cloudwidget/.MainActivity','-f','0x10008000'],capture_output=True,check=True)
    (OUT/(name+'.txt')).write_text(output,encoding='utf-8')
    expected=re.search(r'Checks passed this run: (\d+)',output)
    if result.returncode or 'FAIL ' in output or 'Process crashed' in output or not expected:
        print(output,flush=True);raise SystemExit('Failed '+name)
    passes=len(re.findall(r'^PASS ',output,re.MULTILINE))
    if passes!=int(expected.group(1)):raise SystemExit('Inconsistent check count in '+name)
    print(f'PASS {name}: {passes} checks',flush=True)
