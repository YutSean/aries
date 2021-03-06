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

import org.apache.aries.common.BoolParameter;
import org.apache.aries.common.EnumParameter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringArrayParameter;

import java.util.List;

@SuppressWarnings("rawtypes")
public class GrantPhoenixAccess extends GrantAccessControl {

  private final Parameter<Enum> gv =
      EnumParameter.newBuilder("gpa.grant_revoke", G_V.G, G_V.class)
                   .setDescription("Grant or revoke permission, set either G or V").setRequired().opt();
  private final Parameter<String[]> p_users =
      StringArrayParameter.newBuilder("gpa.users").setDescription("Users who want to access phoenix, delimited by ','").setRequired().opt();
  private final Parameter<Boolean> test_db =
      BoolParameter.newBuilder("gpa.acquire_create_priviledge", false).setDescription("Whether to grant permission to the user for creating tables him/herself").opt();

  @Override protected void requisite(List<Parameter> requisites) {
    requisites.add(p_users);
    requisites.add(gv);
    requisites.add(test_db);
  }

  @Override protected void midCheck() {
    relation.setValue(RELATION.MULTI2MULTI);
    tables.setValue(new String[] { "SYSTEM:CATALOG", "SYSTEM:STATS" });
    permissions.setValue(test_db.value() ? new String[] { "RXW", "R" } : new String[] { "RX", "R" });
    users.setValue(p_users.value());
    g_v.setValue(gv.value());

    super.midCheck();
  }

  @Override
  protected void exampleConfiguration() {
    example(gv.key(), "G");
    example(p_users.key(), "whoever");
    example(test_db.key(), "false");
  }

  @Override protected String getParameterPrefix() {
    return "gpa";
  }
}
