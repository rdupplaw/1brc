/*
 *  Copyright 2023 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package dev.morling.onebrc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalculateAverage_rdupplaw {
    private record Measurement(String station, double temperature) {
        static Measurement from(String line) {
            int delimiterIndex = line.indexOf(';');
            String station = line.substring(0, delimiterIndex);
            double temperature = Double.parseDouble(line.substring(delimiterIndex + 1));
            return new Measurement(station, temperature);
        }
    };

    public static void main(String[] args) throws IOException {
        try (Stream<String> lines = Files.lines(Path.of("./measurements.txt"))) {
            StringBuilder output = new StringBuilder("{");

            lines.parallel()
                    .map(Measurement::from)
                    .collect(Collectors.groupingByConcurrent(
                            Measurement::station,
                            ConcurrentSkipListMap::new,
                            Collectors.summarizingDouble(Measurement::temperature)))
                    .forEach(
                            (k, v) -> {
                                output.append("%s=%.1f/%.1f/%.1f".formatted(k, v.getMin(), v.getAverage(), v.getMax()));
                                output.append(", ");
                            });

            output.delete(output.length() - 2, output.length());

            output.append("}");

            System.out.println(output);
        }
    }
}
