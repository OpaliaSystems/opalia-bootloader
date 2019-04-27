package systems.opalia.bootloader

import org.eclipse.aether.artifact.DefaultArtifact
import scala.util.Properties


object ArtifactNameBuilder {

  sealed class ArtifactNameWithoutVersion(val groupId: String, val artifactId: String, val autoCrossVersion: Boolean) {

    if (!validateNamePart(groupId))
      throw new IllegalArgumentException("Incorrect characters in group id.")

    if (!validateNamePart(artifactId))
      throw new IllegalArgumentException("Incorrect characters in artifact id.")

    def %(version: String): DefaultArtifact = {

      if (!validateNamePart(version))
        throw new IllegalArgumentException("Incorrect characters in version.")

      if (autoCrossVersion)
        new DefaultArtifact(groupId, s"${artifactId}_${crossVersion}", "jar", version)
      else
        new DefaultArtifact(groupId, artifactId, "jar", version)
    }

    private def crossVersion: String =
      """^(\d+\.\d+)""".r.findFirstIn(Properties.versionNumberString).get

    private def validateNamePart(part: String): Boolean =
      part.codePoints().allMatch(x => !Character.isWhitespace(x) || x != (":").codePointAt(0))
  }

  implicit class StringToArtifactName(groupId: String) {

    def %(artifactId: String): ArtifactNameWithoutVersion =
      new ArtifactNameWithoutVersion(groupId, artifactId, autoCrossVersion = false)

    def %%(artifactId: String): ArtifactNameWithoutVersion =
      new ArtifactNameWithoutVersion(groupId, artifactId, autoCrossVersion = true)
  }

}
