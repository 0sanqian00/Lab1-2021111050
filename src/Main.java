package org.example;

import javax.swing.*;
import java.io.*;
import java.util.*;
public class Main {
    private static Map<String, Set<String>> graph;
    private static Map<String, Integer> wordFrequency;
    private static Map<String, Map<String, Integer>> edgeWeights;

    private static volatile boolean stopRandomWalk = false;// 增加一个 volatile 标记来控制随机游走的停止
    // 定义一个Random实例以便在类中重复使用
    private static final Random random = new Random();
    private static int V = 0; // 最大顶点数
    private static int[][] dist; // 保存最短路径长度

    public static void main(String[] args) {
        // 初始化图和单词频率映射
        graph = new HashMap<>();
        wordFrequency = new HashMap<>();
        edgeWeights = new HashMap<>();

        // 读取文本文件并构建图
        readTextFileAndBuildGraph("C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Lab1\\test\\test1.txt");

        Scanner scanner = new Scanner(System.in);
        char choice;

        do {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Show Directed Graph");
            System.out.println("2. Query Bridge Words");
            System.out.println("3. Generate New Text");
            System.out.println("4. Calculate Shortest Path");
            System.out.println("5. Perform Random Walk");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");
            choice = scanner.next().charAt(0);
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case '1' -> {
                    System.out.println("\nGraph built. Displaying directed graph...");
                    showDirectedGraph();
                }
                case '2' -> {
                    System.out.print("Enter word 1: ");
                    String word1 = scanner.nextLine();
                    System.out.print("Enter word 2: ");
                    String word2 = scanner.nextLine();
                    System.out.println("Bridge words from '" + word1 + "' to '" + word2 + "': " + queryBridgeWords(word1, word2));
                }
                case '3' -> {
                    System.out.println("Enter a line of text to generate new text:");
                    String inputText = scanner.nextLine();
                    System.out.println("Generated new text: " + generateNewText(inputText));
                }
                case '4' -> {
                    System.out.print("Enter word 1: ");
                    String wordA = scanner.nextLine();
                    System.out.print("Enter word 2: ");
                    String wordB = scanner.nextLine();
                    showDirectedGraphWithShortestPath(wordA, wordB);
                }
                case '5' -> {
                    System.out.println("\nPerforming a random walk... Press 's' to stop.");
                    stopRandomWalk = false; // 重置停止标志
                    // 使用线程执行随机游走
                    Thread randomWalkThread = new Thread(Main::randomWalk);
                    randomWalkThread.start();

                    // 等待用户输入以停止游走或返回主菜单
                    while (!stopRandomWalk) {
                        System.out.print("Enter 's' to stop the random walk or just press enter to continue: \n");
                        String input = scanner.nextLine().trim().toLowerCase();
                        if ("s".equals(input)) {
                            stopRandomWalk = true; // 用户请求停止随机游走
                            break;
                        }
                    }
                }
                case '6' -> System.out.println("Exiting program.");
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 6.");
            }
        } while (choice != '6');

        scanner.close();

        //
    }


    public static String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        Set<String> bridgeWords = new HashSet<>();
        Set<String> successorsWord1 = graph.get(word1);
        Set<String> predecessorsWord2 = new HashSet<>(graph.size()); // 记录word2的前驱节点

        // 寻找word2的前驱节点
        for (String key : graph.keySet()) {
            Set<String> successors = graph.get(key);
            if (successors.contains(word2)) {
                predecessorsWord2.add(key);
            }
        }

        // 遍历word1的所有后继节点，检查是否也是word2的前驱节点
        for (String successor : successorsWord1) {
            if (predecessorsWord2.contains(successor)) {
                bridgeWords.add(successor);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: " +
                    String.join(", ", bridgeWords);
        }
        //
        //
        //
    }

    private static void readTextFileAndBuildGraph(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 转换为小写，并将非字母字符替换为空格
                line = line.toLowerCase().replaceAll("[^a-z ]", " ");

                // 分一个或多个连续的空白字符来分割字符串
                String[] words = line.split("\\s+");

                for (int i = 0; i < words.length - 1; i++) {
                    String currentWord = words[i];
                    String nextWord = words[i + 1];

                    // 计算每个单词的出现频率。如果currentWord不在映射中，则默认为0，然后加1。
                    wordFrequency.put(currentWord, wordFrequency.getOrDefault(currentWord, 0) + 1);

                    // 添加边和更新权重 如果不存在则创建一个新的HashSet 将nextWord添加到这个HashSet中
                    graph.computeIfAbsent(currentWord, k -> new HashSet<>()).add(nextWord);
                    // updateWeight方法更新图中两个单词之间边的权重
                    updateWeight(currentWord, nextWord);
                }
            }
            // 获取图的顶点数
            V = graph.size();
            // 初始化邻接矩阵
            dist = new int[V][V];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateWeight(String from, String to) {
        // 获取从from到to的现有权重，如果没有设置，则默认为0
        int currentWeight = edgeWeights.getOrDefault(from, new HashMap<>()).getOrDefault(to, 0);

        // 权重加1，因为每次调用此方法意味着A和B又相邻出现了一次
        currentWeight += 1;

        // 更新边的权重
        edgeWeights.computeIfAbsent(from, k -> new HashMap<>()).put(to, currentWeight);
    }

    // 展示有向图
    public static void showDirectedGraph() {
        // DOT 文件将被创建在用户目录下
        String dotFilePath = "graph.dot";
        String graphvizPath = "C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Graphviz-11.0.0-win64\\bin\\dot.exe";
        String pngFilePath = "C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Lab1\\graph.png";

        // 创建DOT文件
        try (PrintWriter out = new PrintWriter(new FileWriter(dotFilePath))) {
            out.println("digraph G {");
            out.println("  rankdir=LR;"); // 设置图的方向从左到右

            // 添加节点
            for (String node : graph.keySet()) {
                out.println("  \"" + escapeDotString(node) + "\" [shape=circle];");
            }

            // 添加边和权重
            for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
                String fromNode = entry.getKey();
                for (String toNode : entry.getValue()) {
                    // 获取边的权重
                    int weight = edgeWeights.getOrDefault(fromNode, new HashMap<>()).getOrDefault(toNode, 0);
                    // 将权重作为标签添加到边
                    out.printf("  \"%s\" -> \"%s\" [label=\"%d\"];\n", escapeDotString(fromNode), escapeDotString(toNode), weight);
                }
            }

            out.println("}");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 使用Graphviz命令行工具生成图形
        try {
            // 构建dot命令
            String command = graphvizPath + " -Tpng " + dotFilePath + " -o " + pngFilePath;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            System.out.println("Graph visualization generated as '" + pngFilePath + "'");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 转义DOT语言特殊字符
    private static String escapeDotString(String input) {
        return input.replace("\"", "\\\"");
    }
