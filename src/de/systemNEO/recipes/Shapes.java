package de.systemNEO.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public abstract class Shapes {
	
	public static ItemStack[][] getFixedShape(ItemStack[] stacks) {
		
		ItemStack[][] shape = {
				{stacks[1],stacks[2],stacks[3]},
				{stacks[4],stacks[5],stacks[6]},
				{stacks[7],stacks[8],stacks[9]}
		};
		
		return shape;
	}
	
	public static ItemStack[][] getFreeShape(ItemStack[] stacks) {
		
		ItemStack[][] shape = Stacks.getDefaultStack();
		
		List<String> liste = new ArrayList<String>();
		HashMap<String,ItemStack> merker = new HashMap<String,ItemStack>();
		
		String stackString = "";
		
		for (int i = 1; i <= 9; ++i) {
		
			if(!Stacks.isStack(stacks[i])) continue;
			
			stackString = Stacks.stackToString(stacks[i]) + "-" + i;
			
			merker.put(stackString, stacks[i]);
			liste.add(stackString);
		}
		
		Collections.sort(liste);
		
		int row = 0;
		int column = 0;
		
		for(String index : liste) {
			
			shape[row][column] = merker.get(index);
			
			++column;
			
			// Letzte Spalte erreicht? Dann auf erste zuruecksetzen und naechste Zeile setzen.
			if(column == 3) {
				column = 0;
				++row;
			}
		}
		
		return shape;
	}
	
	public static ItemStack[][] getVariableShape(ItemStack[] stacks) {
		
		// Um Resourcen zu sparen, Werte der Stacks einmal merken.
		Boolean[] isMat = Stacks.getHasMat(stacks);
		
		// Start und Ende der Rezepte berechnen, leere Zeilen sollen
		// moeglichst ignoriert werden.
		int startCount = getStartCount(isMat);
		int endCount   = getEndCount(isMat);
		
		// Default setzen
		ItemStack[][] shape = Stacks.getDefaultStack();
		
		//Startwerte fuer Zeile und Spalte initieren
		int row = 0;
		int column = 0;
		
		for(int i = startCount; i <= endCount; ++i) {
			
			shape[row][column] = stacks[i];
			
			++column;
			
			// Letzte Spalte erreicht? Dann auf erste zuruecksetzen und naechste Zeile setzen.
			if(column == 3) {
				column = 0;
				++row;
			}
		}
		
		return shape;
	}
	
	public static Integer getStartCount(Boolean[] isMat) {
		
		int startCount = 1;
		
		// Erste Zeile leer?
		if(!isMat[1] && !isMat[2] && !isMat[3]) {
			
			startCount = startCount + 3;
			
			// Mittlere Zeile leer (aber nur checken, wenn die Erste schon leer war)?
			if(!isMat[4] && !isMat[5] && !isMat[6]) startCount = startCount + 3;
		}
		
		// Erste Spalte leer?
		if(!isMat[1] && !isMat[4] && !isMat[7]) {
			
			startCount = startCount + 1;
			
			// Mittlere Spalte leer (aber nur checken, wenn die Erste schon leer war)?
			if(!isMat[2] && !isMat[5] && !isMat[8]) startCount = startCount + 1;
		}
		
		return startCount;
	}
	
	public static Integer getEndCount(Boolean[] isMat) {
		
		int endCount = 9;
		
		// Letzte Zeile leer?
		if(!isMat[7] && !isMat[8] && !isMat[9]) {
			
			endCount = endCount - 3;
			
			// Mittlere Zeile leer (aber nur checken, wenn die Letzte schon leer war)?
			if(!isMat[4] && !isMat[5] && !isMat[6]) endCount = endCount - 3;
		}
		
		// Letzte Spalte leer?
		if(!isMat[3] && !isMat[6] && !isMat[9]) {
			
			endCount = endCount - 1;
			
			// Mittlere Spalte leer (aber nur checken, wenn die Letzte schon leer war)?
			if(!isMat[2] && !isMat[5] && !isMat[8]) endCount = endCount - 1;
		}
		
		return endCount;
	}
	
	/**
	 * @param shape
	 * 			Der zu serialisierende Shape.
	 * @return
	 * 			Liefert eine aus dem Shape abgeleitete Zeichenkette.
	 */
	public static String shapeToString(ItemStack[][] shape) {
		
		StringBuilder shapeString = new StringBuilder();
		
		for(int row = 0; row <= 2; ++row) {
		
			for(int column = 0; column <= 2; ++column) {
				
				if(shapeString.length() > 0) shapeString.append(",");
				shapeString.append(Stacks.stackToString(shape[row][column]));
			}
		}
		
		return shapeString.toString();
	}
	
	public static boolean compareShapes(ItemStack[][] recipeShape, ItemStack[][] craftShape) {
		
		ItemStack craftStack = null;
		ItemStack recipeStack = null;
		
		// Passend zum recipeShape und craftingShape muss nun nachgeschaut
		// werden ob genuegend Items vorhanden sind.
		for(int row = 0; row <= 2; ++row) {
			for(int column = 0; column <= 2; ++column) {
				
				craftStack = craftShape[row][column];
				recipeStack = recipeShape[row][column];
				
				// Wenn im Rezept nix an dem Slot ist (Luft = nix), dann skippen
				if(!Stacks.isStack(recipeStack)) continue;
				
				// Rezept Amount muss hoeher gleich dem Craft Amount sein
				if(recipeStack.getAmount() > craftStack.getAmount()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static Integer[][] getFixedSlotMatrix() {
		
		Integer[][] slotMatrix = {
			{1,2,3},
			{4,5,6},
			{7,8,9}
		};
		
		return slotMatrix;
	}
	
	public static Integer[][] getVariableSlotMatrix(ItemStack[] craftStacks) {
		
		Integer[][] slotMatrix = {{-1,-1,-1},{-1,-1,-1},{-1,-1,-1}};
		
		// Um Resourcen zu sparen, Werte der Stacks einmal merken.
		Boolean[] isMat = Stacks.getHasMat(craftStacks);
		
		// Start und Ende der Rezepte berechnen, leere Zeilen sollen
		// moeglichst ignoriert werden.
		int startCount = Shapes.getStartCount(isMat);
		int endCount   = Shapes.getEndCount(isMat);
		int row        = 0;
		int col        = 0;
		
		for(int i = startCount; i <= endCount; ++i) {
			
			slotMatrix[row][col] = i;
			
			++col;
			
			if(col == 3) {
				col = 0;
				++row;
			}
		}
		
		return slotMatrix;
	}
	
	@SuppressWarnings("deprecation")
	public static Integer[][] getFreeSlotMatrix(ItemStack[] craftStacks, String recipeIndex) {
		
		// Da jede Zutat nur einmal vorkommen darf, muss im Grunde nur vom Rezeptshape aus
		// gesehen in craftStacks geschaut werden, wo sich die jeweilige Zutat befindet.
		Integer[][] slotMatrix = {{-1,-1,-1},{-1,-1,-1},{-1,-1,-1}};
		HashMap<String,Integer> translation = new HashMap<String,Integer>();
		
		for(int i = 1; i <= 9; ++i) translation.put(Stacks.stackToString(craftStacks[i]), i);
		
		ItemStack[][] recipeShape = getRecipeShape(recipeIndex);
		ItemStack shapeStack;
		
		for(int row = 0; row <= 2; ++row) {
			for(int col = 0; col <= 2; ++col) {
				
				shapeStack = recipeShape[row][col];
				
				if(shapeStack.getTypeId() == 0) continue;
				
				slotMatrix[row][col] = translation.get(Stacks.stackToString(shapeStack));
			}
		}
		
		return slotMatrix;
	}
	
	public static void setRecipeShape(String group, String index, ItemStack[][] shape) {
		
		Constants.RECIPES_SHAPE.put(group.toLowerCase() + "_" + index, shape);
	}
	
	public static ItemStack[][] getRecipeShape(String groupIndex) {
		
		return Constants.RECIPES_SHAPE.get(groupIndex);
	}
}
