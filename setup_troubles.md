## Setup Troubleshooting

### Git submodules
If, when you try and set up the project, and get an error like `Configuration with name 'default' not found.`, simply run `git submodule init | git submodule update`, then re-run the setup commands

### Importing the project correctly in IDEA
After cloning the project and ':setupDecompWorkspace', close your currently opened project on IDEA (if any) so that you have the main window of IDEA.
**If you do not have the Lombok Plugin installed**, install it from within IDEA. Restart IDEA.

Select **Import** and find the mod's 'build.gradle'.
Import it and leave everything to default (Auto-Import should be enabled but don't hesitate to check it if it is not)
Once imported, run 'gradlew idea' (from outside IDEA or with the builtin terminal preferrably as the Gradle plugin seems to have troubles)
Finally, in IDEA, go to Settings>Build, Execution, Deployment>Compiler>Annotation Processors and check 'Enable annotation processing' **Without this option, the mod will NOT compile.**

Now, you should be ready to contribute!
