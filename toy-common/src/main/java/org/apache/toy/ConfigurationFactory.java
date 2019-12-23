/*
 * Copyright (c) 2019 R.C
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.toy;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.toy.annotation.Nullable;
import org.apache.toy.annotation.ThreadSafe;
import org.apache.zookeeper.Op;

import java.io.File;
import java.util.Optional;

@ThreadSafe
public final class ConfigurationFactory {

  private enum Project {
    HBASE, KAFKA, JAVA
  }

  private ConfigurationFactory() {}

  /**
   * Create hbase configuration.
   * @param dir_of_conf_file the parent directory where configuration file located
   * @return wrapped hbase configuration
   */
  public static synchronized final Configuration createHBaseConfiguration(String dir_of_conf_file) {
    @Nullable Optional<File> config_file = getConfigurationFileFor(dir_of_conf_file, Project.HBASE);
    if (!config_file.isPresent()) {
      throw new RuntimeException("hbase-site.xml doesn't exist");
    }

    Configuration configuration = HBaseConfiguration.create();
    configuration.addResource(new Path("file://") + config_file.get().getAbsolutePath());
    return configuration;
  }

  /**
   * Create java configuration.
   * @param dir_of_conf_file the parent directory where configuration file located
   * @return wrapped java configuration
   */
  public static synchronized final org.apache.toy.Configuration createJavaConfiguration(String dir_of_conf_file) {
    @Nullable Optional<File> config_file = getConfigurationFileFor(dir_of_conf_file, Project.JAVA);
    if (!config_file.isPresent()) {
      throw new RuntimeException("toy-site.conf doesn't exist");
    }

    org.apache.toy.Configuration configuration = org.apache.toy.Configuration.createConfiguration(config_file.get());
    return configuration;
  }

  @Nullable
  private static Optional<File> getConfigurationFileFor(String dir_of_conf_file, Project project) {
    File dir = new File(dir_of_conf_file);
    Optional<File[]> files = Optional.ofNullable(dir.listFiles());
    if (!files.isPresent()) {
      throw new RuntimeException(dir + " maybe not a directory?.");
    }

    for (File file : files.get()) {
      if (project == Project.HBASE && file.getName().contains("hbase-site.xml")) return Optional.of(file);
      else if (project == Project.JAVA && file.getName().contains("toy-site.conf")) return Optional.of(file);
    }
    return Optional.empty();
  }

}
