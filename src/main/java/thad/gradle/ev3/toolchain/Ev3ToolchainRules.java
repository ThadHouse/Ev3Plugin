package thad.gradle.ev3.toolchain;

import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.toolchain.ToolchainExtension;
import thad.gradle.ev3.Ev3Plugin;

public class Ev3ToolchainRules extends RuleSource {

  @Mutate
    void addDefaultPlatforms(final ExtensionContainer extContainer, final PlatformContainer platforms) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        if (ext.registerPlatforms) {
            NativePlatform ev3 = platforms.maybeCreate(Ev3Plugin.platform, NativePlatform.class);
            ev3.architecture("arm");
            ev3.operatingSystem("linux");
        }
    }
}
