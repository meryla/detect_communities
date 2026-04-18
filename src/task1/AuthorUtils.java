// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class AuthorUtils {

   // We keep a default constructor even though we don't use it explicitly,
   // just to follow standard class structure.
   public AuthorUtils() {
   }

   // This method takes a list of author names and cleans it up.
   // We remove nulls, trim spaces, ignore empty strings,
   // and also make sure there are no duplicates while preserving order.
   public static List<String> cleanAuthors(List<String> var0) {

      // First, we check if the input list is null.
      // If it is, we simply return an empty list to avoid errors later.
      if (var0 == null) {
         return new ArrayList();
      } else {

         // We use a LinkedHashSet because it automatically removes duplicates
         // while keeping the original insertion order.
         LinkedHashSet var1 = new LinkedHashSet();

         // We iterate through each author in the input list.
         for(String var3 : var0) {

            // We skip any null values to avoid NullPointerExceptions.
            if (var3 != null) {

               // We trim the string to remove leading and trailing spaces.
               String var4 = var3.trim();

               // If the result is not empty, we add it to the set.
               // This ensures we only keep meaningful author names.
               if (!var4.isEmpty()) {
                  var1.add(var4);
               }
            }
         }

         // Finally, we convert the set back into a list
         // so the method returns the expected List<String> type.
         return new ArrayList(var1);
      }
   }
}