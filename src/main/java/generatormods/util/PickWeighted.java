/* Source code for the Generator Mods (CARuins, Great Walls, Walled Cities) for the game Minecraft
 * Copyright (C) 2011-2014 by Noah Whitman (formivore) <wakatakeru@gmail.com>
 * Copyright (C) 2013-2014 by Olivier Sylvain (GotoLink) <gotolinkminecraft@gmail.com>
 * Copyright (C) 2014 William (B.J.) Snow Orvis (aetherknight) <aetherknight@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package generatormods.util;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PickWeighted {
    public static <T> T pickWeightedOption(Random random, List<Integer> weights, List<T> options){
        //TODO what if weights and options are different lengths?
        int sum = 0;
        for(int weight : weights)
            sum += weight;
        int choice = random.nextInt(sum);
        sum = 0;
        Iterator<Integer> wi = weights.iterator();
        Iterator<T> wo = options.iterator();
        while (wi.hasNext()) {
            int weight = wi.next();
            T option = wo.next();
            sum += weight;
            if (sum > choice)
                return option;
        }
        return options.get(options.size() - 1);
    }

    public static int pickWeightedOption(Random random, int[] weights, int[] options) {
        int sum = 0, n;
        for (n = 0; n < weights.length; n++)
            sum += weights[n];
        if (sum <= 0) {
            // TODO: report error due to netagive net weight.
            return options[0]; // default to returning first option
        }
        int s = random.nextInt(sum);
        sum = 0;
        n = 0;
        while (n < weights.length) {
            sum += weights[n];
            if (sum > s)
                return options[n];
            n++;
        }
        return options[options.length - 1];
    }

    public static <T> T pickWeightedOption(Random random, int[] weights, T[] options) {
        int sum = 0, n;
        for (n = 0; n < weights.length; n++)
            sum += weights[n];
        if (sum <= 0) {
            // TODO: report error due to netagive net weight.
            return options[0]; // default to returning first option
        }
        int s = random.nextInt(sum);
        sum = 0;
        n = 0;
        while (n < weights.length) {
            sum += weights[n];
            if (sum > s)
                return options[n];
            n++;
        }
        return options[options.length - 1];
    }
}
