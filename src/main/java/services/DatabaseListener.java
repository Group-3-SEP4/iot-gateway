package services;

import models.ConfigModel;

public interface DatabaseListener {
    void configurationReceivedEvent(ConfigModel config);
}
