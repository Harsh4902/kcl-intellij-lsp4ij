package org.example.kclintellijlsp4ij.lsp;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider;

import java.util.List;

public class KCLServer extends ProcessStreamConnectionProvider {

  public KCLServer(Project project) {
    List<String> commands = List.of("kcl-language-server");
    super.setCommands(commands);
  }

}
