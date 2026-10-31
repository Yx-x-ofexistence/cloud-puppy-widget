param([switch]$Upstream,[switch]$Verify,[string]$JdkPath=$env:JAVA_HOME)
$ErrorActionPreference='Stop'
$projectRoot=$PSScriptRoot
if($JdkPath){$env:JAVA_HOME=$JdkPath}
$env:GRADLE_USER_HOME=Join-Path $projectRoot '.tools\gradle-home'
$gradle=Join-Path $projectRoot '.tools\gradle-9.8.2\bin\gradle.bat'
if(!(Test-Path -LiteralPath $gradle)){$gradle=Join-Path $projectRoot 'android\gradlew.bat'}
$target=Join-Path $projectRoot $(if($Upstream){'upstream'}else{'android'})
if(!(Test-Path -LiteralPath $target)){throw "Build target missing: $target"}
$tasks=if($Upstream){@(':app:assembleDebug')}elseif($Verify){@(':app:assembleRelease',':app:assembleDebug',':app:assembleDebugAndroidTest',':app:lintDebug')}else{@(':app:assembleRelease')}
& $gradle -p $target @tasks --no-daemon --max-workers=2
if($LASTEXITCODE -ne 0){throw "Gradle failed with exit $LASTEXITCODE"}
$apk=if($Upstream){Get-ChildItem -LiteralPath (Join-Path $target 'app\build\outputs\apk\debug') -Filter '*.apk' | Select-Object -First 1}else{Get-Item -LiteralPath (Join-Path $target 'app\build\outputs\apk\release\app-release.apk')}
if(!$apk -or $apk.Length -lt 10000){throw 'Expected APK missing'}
if(!$Upstream){
    $out=Join-Path $projectRoot 'artifacts\v0.3.4\CloudPuppyWidget-v0.3.4-candidate-20261030.apk'
    New-Item -ItemType Directory -Path (Split-Path -Parent $out) -Force | Out-Null
    Copy-Item -LiteralPath $apk.FullName -Destination $out -Force
    Get-FileHash -LiteralPath $out -Algorithm SHA256
}
