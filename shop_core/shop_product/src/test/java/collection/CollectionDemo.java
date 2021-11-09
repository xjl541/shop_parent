package collection;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionDemo {
    public static void main(String[] args) {
        Product prod1 =  new Product(1,1,15,"面包","零食");
        Product prod2 = new Product(2, 2, 20, "饼干", "零食");
        Product prod3 = new Product(3, 3, 30, "月饼", "零食");
        Product prod4 = new Product(4, 3, 10, "青岛啤酒", "啤酒");
        Product prod5 = new Product(5, 10, 15, "百威啤酒", "啤酒");

        ArrayList<Product> products = Lists.newArrayList(prod1, prod2, prod3, prod4, prod5);

//        products.forEach(product -> {
//            System.out.println(product);
//        });

//        products.stream().forEach(product -> {
//            System.out.println(product);
//        });
        // 可以将集合中元素根据分类进行分组
//        Map<String, List<Product>> productMap = products.stream().collect(Collectors.groupingBy(Product::getCategory));
//        Set<Map.Entry<String, List<Product>>> entries = productMap.entrySet();
//        entries.forEach(stringListEntry -> {
//            System.out.println(stringListEntry);
//        });
        ArrayList<String> characterList = new ArrayList<>();
        characterList.add("a");
        characterList.add("b");
        characterList.add("c");

        List<String> upperCaseList = characterList.stream().map(character -> {
            String upperCase = character.toUpperCase();
            return upperCase;
        }).collect(Collectors.toList());
        System.out.println(upperCaseList);
    }
}
