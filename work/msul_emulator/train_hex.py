import pefile
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/TormModel.dll',fast_load=True)
print(p.get_data(0x172580,220).hex(' '))
