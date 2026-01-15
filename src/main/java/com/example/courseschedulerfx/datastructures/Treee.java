package com.example.courseschedulerfx.datastructures;

import com.example.courseschedulerfx.model.Department;

// implementing it for department
public class Treee {
    TreeNode root;

    // insert
    public void insert(Department department){
        root = insert(root, department);
    }

    private TreeNode insert(TreeNode root, Department department){
        if(root == null){
            return new TreeNode(department);
        }
        if(department.getDepartmentID() < root.department.getDepartmentID()){
            root.left = insert(root.left, department);
        } else if(department.getDepartmentID() > root.department.getDepartmentID()){
            root.right = insert(root.right, department);
        }
        return root;
    }

    // search
    public Department search(int departmentID){
        return search(root, departmentID);
    }

    private Department search(TreeNode root, int departmentID) {
        if (root == null) {
            return null;
        }
        if (departmentID == root.department.getDepartmentID()) {
            return root.department;
        }
        if (departmentID < root.department.getDepartmentID()) {
            return search(root.left, departmentID);
        } else {
            return search(root.right, departmentID);
        }
    }


}

class TreeNode{
    Department department;
    TreeNode left;
    TreeNode right;

    public TreeNode(Department department){
        this.department = department;
    }
}