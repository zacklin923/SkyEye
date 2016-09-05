package utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.meta_data.FieldMetaData;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility functions for thrift.
 *
 * @author William Farner
 */
public class ThriftUtil {

  /**
   * Pretty-prints a thrift object contents.
   *
   * @param t The thrift object to print.
   * @return The pretty-printed version of the thrift object.
   */
  public static String prettyPrint(TBase t) {
    return t == null ? "null" : printTbase(t, 0);
  }

  /**
   * Prints an object contained in a thrift message.
   *
   * @param o     The object to print.
   * @param depth The print nesting level.
   * @return The pretty-printed version of the thrift field.
   */
  private static String printValue(Object o, int depth) {
    if (o == null) {
      return "null";
    } else if (TBase.class.isAssignableFrom(o.getClass())) {
      return "\n" + printTbase((TBase) o, depth + 1);
    } else if (Map.class.isAssignableFrom(o.getClass())) {
      return printMap((Map) o, depth + 1);
    } else if (List.class.isAssignableFrom(o.getClass())) {
      return printList((List) o, depth + 1);
    } else if (Set.class.isAssignableFrom(o.getClass())) {
      return printSet((Set) o, depth + 1);
    } else if (String.class == o.getClass()) {
      return '"' + o.toString() + '"';
    } else {
      return o.toString();
    }
  }

  private static final String METADATA_MAP_FIELD_NAME = "metaDataMap";

  /**
   * Prints a TBase.
   *
   * @param t     The object to print.
   * @param depth The print nesting level.
   * @return The pretty-printed version of the TBase.
   */
  private static String printTbase(TBase t, int depth) {
    List<String> fields = Lists.newArrayList();
    for (Map.Entry<? extends TFieldIdEnum, FieldMetaData> entry :
            FieldMetaData.getStructMetaDataMap(t.getClass()).entrySet()) {
      @SuppressWarnings("unchecked")
      boolean fieldSet = t.isSet(entry.getKey());
      String strValue;
      if (fieldSet) {
        @SuppressWarnings("unchecked")
        Object value = t.getFieldValue(entry.getKey());
        strValue = printValue(value, depth);
      } else {
        strValue = "not set";
      }
      fields.add(tabs(depth) + entry.getValue().fieldName + ": " + strValue);
    }

    return Joiner.on("\n").join(fields);
  }

  /**
   * Prints a map in a style that is consistent with TBase pretty printing.
   *
   * @param map   The map to print
   * @param depth The print nesting level.
   * @return The pretty-printed version of the map.
   */
  private static String printMap(Map<?, ?> map, int depth) {
    List<String> entries = Lists.newArrayList();
    for (Map.Entry entry : map.entrySet()) {
      entries.add(tabs(depth) + printValue(entry.getKey(), depth)
              + " = " + printValue(entry.getValue(), depth));
    }

    return entries.isEmpty() ? "{}"
            : String.format("{\n%s\n%s}", Joiner.on(",\n").join(entries), tabs(depth - 1));
  }

  /**
   * Prints a list in a style that is consistent with TBase pretty printing.
   *
   * @param list  The list to print
   * @param depth The print nesting level.
   * @return The pretty-printed version of the list
   */
  private static String printList(List<?> list, int depth) {
    List<String> entries = Lists.newArrayList();
    for (int i = 0; i < list.size(); i++) {
      entries.add(
              String.format("%sItem[%d] = %s", tabs(depth), i, printValue(list.get(i), depth)));
    }

    return entries.isEmpty() ? "[]"
            : String.format("[\n%s\n%s]", Joiner.on(",\n").join(entries), tabs(depth - 1));
  }

  /**
   * Prints a set in a style that is consistent with TBase pretty printing.
   *
   * @param set   The set to print
   * @param depth The print nesting level.
   * @return The pretty-printed version of the set
   */
  private static String printSet(Set<?> set, int depth) {
    List<String> entries = Lists.newArrayList();
    for (Object item : set) {
      entries.add(
              String.format("%sItem = %s", tabs(depth), printValue(item, depth)));
    }

    return entries.isEmpty() ? "{}"
            : String.format("{\n%s\n%s}", Joiner.on(",\n").join(entries), tabs(depth - 1));
  }

  private static String tabs(int n) {
    return Strings.repeat("  ", n);
  }

}