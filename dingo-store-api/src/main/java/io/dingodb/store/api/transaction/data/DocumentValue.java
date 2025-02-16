/*
 * Copyright 2021 DataCanvas
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

package io.dingodb.store.api.transaction.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class DocumentValue {

    private ScalarFieldType fieldType;
    private ScalarField fieldValue;

    public enum ScalarFieldType {
        NONE(0),
        BOOL(1),
        INTEGER(4),
        LONG(5),
        FLOAT(6),
        DOUBLE(7),
        STRING(8),
        BYTES(9),
        DATETIME(10);

        private final int code;

        ScalarFieldType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }


    }
}
