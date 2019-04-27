@echo off
java -version
cls


if ERRORLEVEL 9009 (
	echo 找不到java.exe,请检查环境变量配置,
	echo 或立即安装Java虚拟机.
	echo JRE下载地址:http://java.sun.com
	goto end
)

echo.

if ERRORLEVEL 1 (
	if NOT EXIST bin\mainc.class (
		echo 程序文件安装不完整
		echo 详细情况与作者联系.
		goto end
	)
	
	echo 当前的Java版本:
	java -version
	echo.
	echo 运行WebDrome需要的最低版本: java version "1.6.0_03"
	echo.
	echo 请安装最新的Java VM: http://java.sun.com
	goto end
)

echo.

echo	环境测试正常,可以运行WebDrome服务器..
echo.
echo.

:end
echo.
echo	-------------------------------- CatfoOD 2008 ----------------------------------
echo	email: yanming-sohu@sohu.com
echo	QQ: 412475540
echo.
echo	Enjoyment.
echo.
echo.
echo.
echo.
echo.

echo 按任意键--关闭窗口...
pause > nul