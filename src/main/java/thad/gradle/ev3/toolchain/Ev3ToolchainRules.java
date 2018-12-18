package thad.gradle.ev3.toolchain;

import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.toolchain.ToolchainExtension;

public class Ev3ToolchainRules extends RuleSource {

  @Mutate
    void addDefaultPlatforms(final ExtensionContainer extContainer, final PlatformContainer platforms) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        if (ext.registerPlatforms) {
            NativePlatform roborio = platforms.maybeCreate("linuxev3", NativePlatform.class);
            roborio.architecture("arm");
            roborio.operatingSystem("linux");
        }
    }
}
