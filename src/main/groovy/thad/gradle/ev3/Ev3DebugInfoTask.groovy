package thad.gradle.ev3

import jaci.gradle.deploy.DeployExtension
import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import jaci.gradle.deploy.artifact.Artifact
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

@CompileStatic
class Ev3DebugInfoTask extends DefaultTask {

    @TaskAction
    void writeDebugInfo() {
        def cfg = []
        project.extensions.getByType(DeployExtension).artifacts.all { Artifact art ->
          if (art instanceof Ev3NativeArtifact) {
                art.targets.all { String target ->
                    cfg << [
                        artifact: art.name,
                        target: target,
                        component: (art as Ev3NativeArtifact).component,
                        debugfile: "${art.name}_${target}.debugconfig".toString(),
                        language: "cpp"
                    ]
                }
            }
        }

        def file = new File(project.buildDir, "debug/debuginfo.json")
        file.parentFile.mkdirs()

        def gbuilder = new GsonBuilder()
        gbuilder.setPrettyPrinting()
        file.text = gbuilder.create().toJson(cfg)
    }

}
