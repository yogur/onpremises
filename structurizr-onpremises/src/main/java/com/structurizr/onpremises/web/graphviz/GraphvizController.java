package com.structurizr.onpremises.web.graphviz;

import com.structurizr.Workspace;
import com.structurizr.graphviz.GraphvizAutomaticLayout;
import com.structurizr.graphviz.RankDirection;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.api.ApiController;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;

@RestController
public class GraphvizController {

    private static final Log log = LogFactory.getLog(GraphvizController.class);

    @PostMapping(value = "/graphviz", consumes = "application/json", produces = "application/json; charset=UTF-8")
    public String post(@RequestBody String json,
                       @RequestParam(required = false) String view,
                       @RequestParam(required = false, defaultValue = "TopBottom") RankDirection rankDirection,
                       @RequestParam(required = false, defaultValue = "true") boolean resizePaper,
                       @RequestParam(required = false, defaultValue = "300") int rankSeparation,
                       @RequestParam(required = false, defaultValue = "300") int nodeSeparation,
                       @RequestParam(required = false, defaultValue = "400") int margin) {

        try {
            Configuration configuration = Configuration.getInstance();
            if (configuration.isGraphvizEnabled()) {
                Workspace workspace = WorkspaceUtils.fromJson(json);

                if (configuration.hasInternetConnection()) {
                    log.debug("Loading themes");
                    try {
                        ThemeUtils.loadThemes(workspace);
                    } catch (Exception e) {
                        log.warn("Ignoring themes: " + e.getMessage());
                    }
                } else {
                    log.debug("No internet connection - ignoring themes");
                }

                File workspaceDirectory = new File(configuration.getDataDirectory(), "" + workspace.getId());
                File graphvizDirectory = new File(workspaceDirectory, "graphviz");
                graphvizDirectory.mkdirs();
                if (graphvizDirectory.exists()) {
                    File tmpdir = Files.createTempDirectory(graphvizDirectory.toPath(), "").toFile();
                    tmpdir.deleteOnExit();
                    log.debug("Graphviz working directory is " + tmpdir.getAbsolutePath());

                    GraphvizAutomaticLayout graphviz = new GraphvizAutomaticLayout(tmpdir);
                    graphviz.setRankDirection(rankDirection);
                    graphviz.setChangePaperSize(resizePaper);
                    graphviz.setRankSeparation(rankSeparation);
                    graphviz.setNodeSeparation(nodeSeparation);
                    graphviz.setMargin(margin);

                    for (CustomView v : workspace.getViews().getCustomViews()) {
                        if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                            graphviz.apply(v);
                        }
                    }

                    for (SystemLandscapeView v : workspace.getViews().getSystemLandscapeViews()) {
                        if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                            graphviz.apply(v);
                        }
                    }

                    for (SystemContextView v : workspace.getViews().getSystemContextViews()) {
                        if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                            graphviz.apply(v);
                        }
                    }

                    for (ContainerView v : workspace.getViews().getContainerViews()) {
                        if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                            graphviz.apply(v);
                        }
                    }

                    for (ComponentView v : workspace.getViews().getComponentViews()) {
                        if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                            graphviz.apply(v);
                        }
                    }

                    for (DynamicView v : workspace.getViews().getDynamicViews()) {
                        if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                            graphviz.apply(v);
                        }
                    }

                    for (DeploymentView v : workspace.getViews().getDeploymentViews()) {
                        if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                            graphviz.apply(v);
                        }
                    }

                    try {
                        File[] files = tmpdir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                file.delete();
                            }
                        }
                        tmpdir.delete();
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                } else {
                    log.error(graphvizDirectory.getAbsolutePath() + " could not be created; cannot run Graphviz");
                }

                return WorkspaceUtils.toJson(workspace, false);
            } else {
                return json;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}