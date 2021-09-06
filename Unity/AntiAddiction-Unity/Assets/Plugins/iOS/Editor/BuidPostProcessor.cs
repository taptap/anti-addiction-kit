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
#if UNITY_2019_3_OR_NEWER
            string unityFrameworkTarget = project.TargetGuidByName("UnityFramework");
            string targetGUID = project.GetUnityMainTargetGuid();
#else
                string unityFrameworkTarget = project.TargetGuidByName("Unity-iPhone");
                string targetGUID = project.TargetGuidByName("Unity-iPhone");
#endif
            if (targetGUID == null)
            {
                Debug.Log("target is null ?");
                return;
            }

            // Built in Frameworks
            project.AddFrameworkToProject(targetGUID, "Foundation.framework", false);
            project.AddFrameworkToProject(targetGUID, "libz.tbd", false);

            Debug.Log("Added iOS frameworks to project");

            // Add Shell Script to copy folders and files after running successfully
            

            // Add '-Objc' to "Other Linker Flags"
            project.AddBuildProperty(targetGUID, "OTHER_LDFLAGS", "-Objc");
            project.SetBuildProperty(targetGUID, "SWIFT_VERSION", "5.0");

            // Overwrite
            project.WriteToFile(projectPath);
        }
    }
}
