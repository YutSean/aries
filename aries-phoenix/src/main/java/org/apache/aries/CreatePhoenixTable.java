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

package org.apache.aries;

import org.apache.aries.common.*;

import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreatePhoenixTable extends AbstractPhoenixToy {

  private final Set<String> compression_set = new HashSet<>(Arrays.asList("LZO", "GZ", "NONE", "SNAPPY", "LZ4", "BZIP2"));
  private final Set<String> bloom_set = new HashSet<>(Arrays.asList("NONE", "ROW", "ROWCOL"));
  private final String TABLE_OWNERS = "TABLE_OWNERS";
  private final String BLOOMFILTER = "BLOOMFILTER";
  private final String BLOCKSIZE = "BLOCKSIZE";
  private final String COMPRESSION = "COMPRESSION";
  private final String TTL = "TTL";
  private final String SALT_BUCKETS = "SALT_BUCKETS";
  private final String UPDATE_CACHE_FREQUENCY = "UPDATE_CACHE_FREQUENCY";

  private final Parameter<String> sql =
      StringParameter.newBuilder("cpt.create_table_sql").setRequired().setDescription("Create table sql, it can be patterned with %s").opt();
  private final Parameter<String> owners =
      StringParameter.newBuilder("cpt.table_owners").setRequired().setDescription("Owners of table, delimited by ','").opt();
  private final Parameter<Integer> bucket_size =
      IntParameter.newBuilder("cpt.salt_buckets").setDescription("Salted bucket size for table, larger size will increase throughput but introduce latency").opt();
  private final Parameter<String> compression =
      StringParameter.newBuilder("cpt.compression").setDescription("Compression will be used for this table, options are LZO, GZ, SNAPPY, LZ4, BZIP2")
                     .addConstraint(compression_set::contains).opt();
  private final Parameter<String> bloomfilter =
      StringParameter.newBuilder("cpt.bloom_filter").setDefaultValue("NONE").setDescription("Bloom filter will be used in table, options are ROW and ROWCOL")
                     .addConstraint(bloom_set::contains).opt();
  private final Parameter<Integer> time_to_live =
      IntParameter.newBuilder("cpt.time_to_live").setDescription("Time to live for values")
                  .addConstraint(v -> v > 0).opt();
  private final Parameter<String[]> others =
      StringArrayParameter.newBuilder("cpt.attributes")
                          .setDescription("Other attributes: k1=v1,k2=v2,..., please use '' for string attributes").opt();
  private final Parameter<String[]> options =
      StringArrayParameter.newBuilder("cpt.pattern.values")
                          .setDescription("If cpt.create_table_sql uses pattern, then list the optional values here").opt();

  @Override protected void requisite(List<Parameter> requisites) {
    requisites.add(sql);
    requisites.add(owners);
    requisites.add(bucket_size);
    requisites.add(compression);
    requisites.add(bloomfilter);
    requisites.add(time_to_live);
    requisites.add(others);
    requisites.add(options);
  }

  @Override protected int haveFun() throws Exception {
    String create_statement = decorateSQL(sql.value());
    Statement statement = connection.createStatement();
    if (options.empty()) {
      LOG.info("Executing SQL: " + create_statement);
      statement.execute(create_statement);
    } else {
      for (String option : options.value()) {
        String sql = create_statement.replace("%s", option);
        LOG.info("Executing SQL: " + sql);
        statement.execute(sql);
      }
    }

    return RETURN_CODE.SUCCESS.code();
  }

  private String decorateSQL(String sql) {
    StringBuilder
           builder = new StringBuilder(sql);
           builder.append(" ").append(TABLE_OWNERS).append("='").append(owners.value()).append("'");
           builder.append(", ").append(UPDATE_CACHE_FREQUENCY).append("='").append("NEVER").append("'");
           builder.append(", ").append(BLOCKSIZE).append("='").append(Constants.ONE_KB * 32).append("'");
    if (!bucket_size.empty())
           builder.append(", ").append(SALT_BUCKETS).append("=").append(bucket_size.value());
    if (!compression.empty())
           builder.append(", ").append(COMPRESSION).append("='").append(compression.value()).append("'");
    if (!bloomfilter.empty())
           builder.append(", ").append(BLOOMFILTER).append("='").append(bloomfilter.value()).append("'");
    if (!time_to_live.empty())
           builder.append(", ").append(TTL).append("=").append(time_to_live.value());
    if (!others.empty())
      for (String attribute : others.value()) {
        if (attribute.split("=").length != 2) continue;
           builder.append(", ").append(attribute);
      }
    return builder.toString();
  }

  @Override
  protected void exampleConfiguration() {
    example(sql.key(), "CREATE TABLE user_%s (\"user_id\" BIGINT PRIMARY KEY );");
    example(owners.key(), "bob,alice");
    example(bucket_size.key(), "2");
    example(compression.key(), "SNAPPY");
    example(bloomfilter.key(), "ROW");
    example(time_to_live.key(), "259200");
    example(options.key(), "cn,us,sg,jp,uk");
  }

  @Override protected String getParameterPrefix() {
    return "cpt";
  }

}
