#### ADS-B Compact Position Reporting (CPR) algorithms in Java
This is a translation of cpr.c in dump1090, written in Java 8 using Netbeans 8 IDE.

The built-in tests all pass:
```
run:
testCPRGlobalAirborne[0,EVEN]: PASS
testCPRGlobalAirborne[0,ODD]:  PASS
testCPRGlobalAirborne[1,EVEN]: PASS
testCPRGlobalAirborne[1,ODD]:  PASS

testCPRGlobalSurface[0,EVEN]:  PASS
testCPRGlobalSurface[0,ODD]:   PASS
testCPRGlobalSurface[1,EVEN]:  PASS
testCPRGlobalSurface[1,ODD]:   PASS
testCPRGlobalSurface[2,EVEN]:  PASS
testCPRGlobalSurface[2,ODD]:   PASS
testCPRGlobalSurface[3,EVEN]:  PASS
testCPRGlobalSurface[3,ODD]:   PASS
testCPRGlobalSurface[4,EVEN]:  PASS
testCPRGlobalSurface[4,ODD]:   PASS
testCPRGlobalSurface[5,EVEN]:  PASS
testCPRGlobalSurface[5,ODD]:   PASS
testCPRGlobalSurface[6,EVEN]:  PASS
testCPRGlobalSurface[6,ODD]:   PASS
testCPRGlobalSurface[7,EVEN]:  PASS
testCPRGlobalSurface[7,ODD]:   PASS
testCPRGlobalSurface[8,EVEN]:  PASS
testCPRGlobalSurface[8,ODD]:   PASS
testCPRGlobalSurface[9,EVEN]:  PASS
testCPRGlobalSurface[9,ODD]:   PASS
testCPRGlobalSurface[10,EVEN]:  PASS
testCPRGlobalSurface[10,ODD]:   PASS
testCPRGlobalSurface[11,EVEN]:  PASS
testCPRGlobalSurface[11,ODD]:   PASS
testCPRGlobalSurface[12,EVEN]:  PASS
testCPRGlobalSurface[12,ODD]:   PASS
testCPRGlobalSurface[13,EVEN]:  PASS
testCPRGlobalSurface[13,ODD]:   PASS
testCPRGlobalSurface[14,EVEN]:  PASS
testCPRGlobalSurface[14,ODD]:   PASS
testCPRGlobalSurface[15,EVEN]:  PASS
testCPRGlobalSurface[15,ODD]:   PASS
testCPRGlobalSurface[16,EVEN]:  PASS
testCPRGlobalSurface[16,ODD]:   PASS
testCPRGlobalSurface[17,EVEN]:  PASS
testCPRGlobalSurface[17,ODD]:   PASS
testCPRGlobalSurface[18,EVEN]:  PASS
testCPRGlobalSurface[18,ODD]:   PASS
testCPRGlobalSurface[19,EVEN]:  PASS
testCPRGlobalSurface[19,ODD]:   PASS
testCPRGlobalSurface[20,EVEN]:  PASS
testCPRGlobalSurface[20,ODD]:   PASS
testCPRGlobalSurface[21,EVEN]:  PASS
testCPRGlobalSurface[21,ODD]:   PASS
testCPRGlobalSurface[22,EVEN]:  PASS
testCPRGlobalSurface[22,ODD]:   PASS

testCPRRelative[0]:  PASS
testCPRRelative[1]:  PASS
testCPRRelative[2]:  PASS
testCPRRelative[3]:  PASS
testCPRRelative[4]:  PASS
testCPRRelative[5]:  PASS
testCPRRelative[6]:  PASS
testCPRRelative[7]:  PASS
testCPRRelative[8]:  PASS
testCPRRelative[9]:  PASS
testCPRRelative[10]:  PASS
testCPRRelative[11]:  PASS
testCPRRelative[12]:  PASS
testCPRRelative[13]:  PASS
testCPRRelative[14]:  PASS
testCPRRelative[15]:  PASS
testCPRRelative[16]:  PASS
testCPRRelative[17]:  PASS
testCPRRelative[18]:  PASS
testCPRRelative[19]:  PASS
testCPRRelative[20]:  PASS
testCPRRelative[21]:  PASS
testCPRRelative[22]:  PASS
testCPRRelative[23]:  PASS
testCPRRelative[24]:  PASS
testCPRRelative[25]:  PASS
testCPRRelative[26]:  PASS
testCPRRelative[27]:  PASS
testCPRRelative[28]:  PASS
testCPRRelative[29]:  PASS

Tests Successful
```
