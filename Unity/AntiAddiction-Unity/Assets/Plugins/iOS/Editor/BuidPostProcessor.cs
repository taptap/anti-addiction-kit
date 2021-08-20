using UnityEngine;
using UnityEditor;
using UnityEditor.Callbacks;
using UnityEditor.iOS.Xcode;
using System.IO;
using System.Linq;

public class BuildPostProcessor
{
    [PostProcessBuild]
    public static void OnPostProcessBuild(BuildTarget target, string path)
    {
        if (target == BuildTarget.iOS)
        {
            var projectPath = PBXProject.GetPBXProjectPath(path);
            var project = new PBXProject();
            project.ReadFromFile(projectPath);

            // Get targetGUID
            var targetName = PBXProject.GetUnityTargetName();
            var targetGUID = project.TargetGuidByName(targetName);

            // Built in Frameworks
            project.AddFrameworkToProject(targetGUID, "Foundation.framework", false);

            Debug.Log("Added iOS frameworks to project");

            // Add Shell Script to copy folders and files after running successfully
            

            // Add '-Objc' to "Other Linker Flags"
            project.AddBuildProperty(targetGUID, "OTHER_LDFLAGS", "-Objc");

            // Overwrite
            project.WriteToFile(projectPath);
        }
    }
}
