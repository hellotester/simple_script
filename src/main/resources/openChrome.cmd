title autoplay
set cm1=cd C:\Program Files\Google\Chrome\Application
set cm2=c:
set cm3=chrome.exe --remote-debugging-port=9222 --remote-allow-origins=*
echo now browser start.
%cm1% && %cm2% && %cm3%
echo done


