package systems.opalia.bootloader

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.artifact.{Artifact, DefaultArtifact}
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.{LocalRepository, RemoteRepository}
import org.eclipse.aether.resolution.{ArtifactRequest, DependencyRequest}
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import org.eclipse.aether.{DefaultRepositorySystemSession, RepositorySystem}
import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap


final class ArtifactResolver(remoteRepositories: Seq[RemoteRepository],
                             localRepository: LocalRepository) {

  private val repositorySystem = newRepositorySystem()

  def this(remoteRepositories: ListMap[String, String], localRepository: String) =
    this(
      remoteRepositories.map(x => new RemoteRepository.Builder(x._1, "default", x._2).build()).toSeq,
      new LocalRepository(localRepository)
    )

  def resolve(artifactName: String): Artifact = {

    val session = newRepositorySystemSession()

    val artifactRequest = new ArtifactRequest()

    artifactRequest.setArtifact(new DefaultArtifact(artifactName))
    artifactRequest.setRepositories(remoteRepositories.asJava)

    val artifactResult = repositorySystem.resolveArtifact(session, artifactRequest)

    if (artifactResult.isMissing)
      throw new IllegalArgumentException(s"Cannot resolve artifact “$artifactName”.")

    artifactResult.getArtifact
  }

  def resolveTransitive(artifactName: String,
                        scope: ArtifactResolver.Scope,
                        scopeFilter: Seq[ArtifactResolver.Scope]): Seq[Artifact] = {

    val session = newRepositorySystemSession()

    val collectRequest = new CollectRequest()

    collectRequest.setRoot(new Dependency(new DefaultArtifact(artifactName), scope.toString))
    collectRequest.setRepositories(remoteRepositories.asJava)

    val dependencyRequest =
      new DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(scopeFilter.map(_.toString): _*))

    repositorySystem.resolveDependencies(session, dependencyRequest).getArtifactResults.asScala
      .map {
        artifactResult =>

          if (artifactResult.isMissing)
            throw artifactResult.getExceptions.asScala.head

          artifactResult.getArtifact
      }
  }

  private def newRepositorySystem(): RepositorySystem = {

    val locator = MavenRepositorySystemUtils.newServiceLocator()

    locator.addService(classOf[RepositoryConnectorFactory], classOf[BasicRepositoryConnectorFactory])
    locator.addService(classOf[TransporterFactory], classOf[FileTransporterFactory])
    locator.addService(classOf[TransporterFactory], classOf[HttpTransporterFactory])

    locator.getService(classOf[RepositorySystem])
  }

  private def newRepositorySystemSession(): DefaultRepositorySystemSession = {

    val session = MavenRepositorySystemUtils.newSession()

    session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepository))

    //session.setTransferListener(new ConsoleTransferListener())
    //session.setRepositoryListener(new ConsoleRepositoryListener())

    session
  }
}

object ArtifactResolver {

  sealed abstract class Scope(name: String) {

    override def toString: String =
      name
  }

  object Scope {

    case object Compile
      extends Scope(JavaScopes.COMPILE)

    case object Provided
      extends Scope(JavaScopes.PROVIDED)

    case object System
      extends Scope(JavaScopes.SYSTEM)

    case object Runtime
      extends Scope(JavaScopes.RUNTIME)

    case object Test
      extends Scope(JavaScopes.TEST)

    val values: Seq[Scope] =
      Compile :: Provided :: System :: Runtime :: Test :: Nil

    def withNameOpt(string: String): Option[Scope] =
      values.find(_.toString.equalsIgnoreCase(string))

    def withName(string: String): Scope =
      withNameOpt(string).getOrElse(throw new IllegalArgumentException(s"Cannot find Scope with name “$string”."))
  }

}
