/*
   Copyright 2012-2015 Michael Pozhidaev <msp@altlinux.org>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.commander.i18n;

import java.io.*;

import org.luwrain.app.commander.Operation;
import org.luwrain.app.commander.PanelArea;

public class Ru implements org.luwrain.app.commander.Strings
{
    @Override public String appName()
    {
	return "Обзор файлов и папок";
    }

    @Override public String leftPanel()
    {
	return "Левая панель ";
    }

    @Override public String rightPanel()
    {
	return "Правая панель ";
    }

    @Override public String operationsAreaName()
    {
	return "Действия";
    }

    /*

    public String noItemsAbove()
    {
	return "Элементы выше отсутствуют";
    }

    public String noItemsBelow()
    {
	return "Элементы ниже отсутствуют";
    }

    public String inaccessibleDirectoryContent()
    {
	return "Содержимое каталога недоступно";
    }

    @Override public String rootDirectory()
    {
	return "Корневой каталог";
    }
*/

/*    public String dirItemIntroduction(DirItem item, boolean brief)
    {
	if (item == null)
	    return "";
	String text = item.getFileName();
	if (text.isEmpty())
	    return "fixme";
	if (text.equals(PanelArea.PARENT_DIR))
	    return "На уровень вверх";
	if (!brief)
	{
	    if (item.getType() == DirItem.DIRECTORY)
	    {
		if (item.isSelected())
		    text = "Выделенный каталог " + text; else
		    text = "Каталог " + text;
	    } else
		if (item.selected)
		    text = "Выделенный файл " + text;
	}
	return text;
    }

    @Override public String done()
    {
	return "Завершено";
    }

    @Override public String failed()
    {
	return "Ошибка";
    }

    @Override public String copying(File[] files)
    {
	if (files == null)
	    return "";
	if (files.length == 1)
	    return "Копирование " + files[0].getName();
	return "Копирование " + files + " элемента(ов)";
    }

*/

    @Override public String copyPopupName()
    {
	return "Копирование";
    }

    @Override public String copyPopupPrefix(File[] files)
    {
	if (files == null || files.length < 1)
	    return "";
	if (files.length == 1)
	    return "Копировать \"" + files[0].getName() + "\" в:";
	return "Копировать " + files.length + " элемента(ов) в:";
    }

    @Override public String copyOperationName(File[] filesToCopy, File copyTo)
    {
	if (filesToCopy.length == 1)
	    return "Копирование " + filesToCopy[0].getName() + " в " + copyTo.getAbsolutePath();
	return "Копирование " + numberOfItems(filesToCopy.length) + " в "  + copyTo.getAbsolutePath();
    }

    @Override public String movePopupName()
    {
	return "Переместить/переименовать";
    }

    @Override public String movePopupPrefix(File[] files)
    {
	if (files == null || files.length < 1)
	    return "";
	if (files.length == 1)
	    return "Переместить/переименовать \"" + files[0].getName() + "\" в:";
	return "Переместить " + files.length + " элемента(ов) в:";
    }

    @Override public String mkdirPopupName()
    {
	return "Создание каталога";
    }

    @Override public String mkdirPopupPrefix()
    {
	return "Имя нового каталога:";
    }

    @Override public String delPopupName()
    {
	return "Удаление";
    }

    @Override public String delPopupPrefix(File[] files)
    {
	if (files == null || files.length < 1)
	    return "";
	if (files.length == 1)
	{
	    if (files[0].isDirectory())
	    return "Вы действительно хотите удалить каталог \"" + files[0].getName() + "\"?";
	    return "Вы действительно хотите удалить файл \"" + files[0].getName() + "\"?";
	}
	return "Вы действительно хотите удалить " + files.length + " элемента(ов)?";
    }

    @Override public String operationCompletedMessage(Operation op)
    {
	switch (op.getFinishCode())
	{
	case Operation.OK:
	    return op.getOperationName() + " успешно завершено";
	case Operation.INTERRUPTED:
	    return op.getOperationName() + " отменено";
	default:
	    return op.getOperationName() + " завершилось неуспешно";
	}
    }

    @Override public String operationFinishDescr(Operation op)
    {
	switch (op.getFinishCode())
	{
	case Operation.OK:
	    return op.getOperationName() + ": Готово";
	case Operation.INTERRUPTED:
	    return op.getOperationName() + ": Прервано пользователем";
	default:
	    //FIXME:
	    return op.getOperationName() + ": Ошибка";
	}
    }

    private String numberOfItems(int num)
    {
	return "" + num + " " + afterNum(num, "элементов", "элемент", "элемента");
    }

    private String numberOfFiles(int num)
    {
	return "" + num + " " + afterNum(num, "файлов", "файл", "файла");
    }

    private String afterNum(int num,
				  String afterZero,
				  String afterOne,
				  String afterTwo)
    {
	if (num < 0)
	    throw new IllegalArgumentException("num may not be negative");
	if (afterZero == null)
	    throw new NullPointerException("afterZero may not be null");
	if (afterOne == null)
	    throw new NullPointerException("afterOne may not be null");
	if (afterTwo == null)
	    throw new NullPointerException("afterTwo may not be null");
	if (num == 0 || num % 10 == 0)
	    return afterZero;
	if (num % 100 >= 11 && num % 100 <= 19)
	    return afterZero;
	if (num % 10 == 1)
	    return afterOne;
	if (num % 10 >= 2 && num % 10 < 4)
	    return afterTwo;
	return afterZero;
    }
}
