Create launcher in Sloeber

CopySymphony Launcher
1. Click on Run-->External Tools-->External Tools Configuration
2. Click on New launch Configuration
3. In the Main tab, Set the following Name: CopySymphony, Location: click on "Browse File System", locate copySymphony.bat from the workspace. Click Open. Arguments: ${workspace_loc}\symphony {Arduino Symphony library directory} C:\Users\miras.DESKTOP-JHPGKS4\OneDrive\Documents\Arduino\libraries\Symphony in my case {Arduino Symphony library directory} = C:\Users\miras.DESKTOP-JHPGKS4\OneDrive\Documents\Arduino\libraries\Symphony
4. In the Build tab, uncheck "Build before Launch"

CompileWithVersionUp Launcher
1. Click on Run-->External Tools-->External Tools Configuration
2. Click on New launch Configuration
3. Set the following Name: CompileWithVersionUp, Location: click on "Browse File System", locate compile.bat from the workspace. Click Open. Arguments: ${workspace_loc}${project_path}
4. In the Build tab, check "Build before Launch" the select "The project containing the selected resource"

MakeBin Launcher
This executes the python3 command that is an error in the current make for core v2.7.1
Execute compile first, then run this to create the bin file.
This increments the version.
1. Click on Run-->External Tools-->External Tools Configuration
2. Click on New launch Configuration
3. Set the following Name: MakeBin, Location: click on "Browse File System", locate makeBin.bat from the workspace. Click Open. Arguments: ${workspace_loc}${project_path} ${project_name}
4. In the Build tab, uncheck "Build before Launch"

runGulp Launcher
1. Click on Run-->External Tools-->External Tools Configuration
2. Click on New launch Configuration
3. In the Main tab, Set the following Name: runGulp, Location: ${project_path}\cmd Arguments: gulp
4. In the Build tab, uncheck "Build before Launch"
