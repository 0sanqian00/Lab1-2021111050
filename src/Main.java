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
    }