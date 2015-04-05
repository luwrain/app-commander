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

    @Override public String copyPopupName()
    {
	return "Копирование";
    }

    @Override public String copyPopupPrefix(File[] files)
    {
	if (files == null)
	    throw new NullPointerException("files may not be null");
	if (files.length == 1)
	    return "Копировать " + files[0].getName() + " в:";
	return "Копировать " + numberOfItems(files.length) + " в:";
    }

    @Override public String copyOperationName(File[] filesToCopy, File copyTo)
    {
	if (filesToCopy.length == 1)
	    return "Копирование " + filesToCopy[0].getName() + " в " + copyTo.getAbsolutePath();
	return "Копирование " + numberOfItems(filesToCopy.length) + " в "  + copyTo.getAbsolutePath();
    }

    @Override public String movePopupName()
    {
	return "Переименование/перемещение";
    }

    @Override public String movePopupPrefix(File[] files)
    {
	if (files == null)
	    throw new NullPointerException("files may not be null");
	if (files.length == 1)
	    return "Переименовать/переместить " + files[0].getName() + " в:";
	return "Переименовать/переместить " + numberOfItems(files.length) + " в:";
    }

    @Override public String moveOperationName(File[] filesToMove, File moveTo)
    {
	if (filesToMove.length == 1)
	    return "Переименование/перемещение " + filesToMove[0].getName() + " в " + moveTo.getAbsolutePath();
	return "Переименование/перемещение " + numberOfItems(filesToMove.length) + " в "  + moveTo.getAbsolutePath();
    }

    @Override public String mkdirPopupName()
    {
	return "Создание каталога";
    }

    @Override public String mkdirPopupPrefix()
    {
	return "Имя нового каталога:";
    }

    @Override public String mkdirErrorMessage()
    {
	return "Каталог по указанному пути не может быть создан";
    }

    @Override public String mkdirOkMessage()
    {
	return "Каталог успешно создан";
    }

    @Override public String delPopupName()
    {
	return "Удаление";
    }

    @Override public String delPopupText(File[] files)
    {
	if (files == null)
	    throw new NullPointerException("files may not be null");
	if (files.length == 1)
	    return "Вы действительно хотите удалить " + files[0] + "?";
	return "Вы действительно хотите удалить " + numberOfItems(files.length) + "?";
    }

    @Override public String delOperationName(File[] filesToDelete)
    {
	if (filesToDelete.length == 1)
	    return "Удаление " + filesToDelete[0].getName();
	return "Удаление " + numberOfItems(filesToDelete.length);
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
	    return "Готово: " + op.getOperationName();
	case Operation.INTERRUPTED:
	    return "Прервано: " + op.getOperationName();
	case Operation.COPYING_NON_FILE_TO_FILE:
	    return "Команда копировать не в файл в файл: " + op.getOperationName();
	case Operation.PROBLEM_OPENING_FILE:
	    return "Ошибка открытия файла " + op.getExtInfo() + ": " + op.getOperationName();
	case Operation.PROBLEM_CREATING_FILE:
	    return "Ошибка создания файла " + op.getExtInfo() + ": " + op.getOperationName();
	case Operation.PROBLEM_READING_FILE:
	    return "Ошибка чтения файла " + op.getExtInfo() + ": " + op.getOperationName();
	case Operation.PROBLEM_WRITING_FILE:
	    return "Ошибка записи файла " + op.getExtInfo() + ": " + op.getOperationName();
	case Operation.INACCESSIBLE_SOURCE:
	    return "Элементы для копирования недоступны в полном объёме: " + op.getOperationName();
	case Operation.PROBLEM_CREATING_DIRECTORY:
	    return "Невозможно создать каталог " + op.getExtInfo() + ": " + op.getOperationName();
	case Operation.UNEXPECTED_PROBLEM:
	    return "Неожиданная ошибка: " + op.getOperationName();
	default:
	    return "Неизвестная ошибка: " + op.getOperationName();
	}
    }

    @Override public String notAllOperationsFinished()
    {
	return "Приложение не может быть закрыто при выполняющихся действиях";
    }

    @Override public String cancelOperationPopupName()
    {
	return "Отмена действия";
    }

    @Override public String cancelOperationPopupText(Operation op)
    {
	return "Вы действительно хотите отменить действие \"" + op.getOperationName() + "\"?";
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

    @Override public String bytesNum(long num)
    {
	if (num > 1024 * 1024 * 1024)
	{
	    final long g = num / (1024 * 1024 * 1024);
	    long rest = num - (g * 1024 * 1024 * 1024);
	    rest /= (102 * 1024 * 1024);
	    return g + "," + rest + "ГБ";
	}
	if (num > 1024 * 1024)
	{
	    final long m = num / (1024 * 1024);
	    long rest = num - (m * 1024 * 1024);
	    rest /= (102 * 1024);
	    return m + "," + rest + "МБ";
	}
	if (num > 1024)
	{
	    final long k = num / 1024;
	    long rest = num - (k * 1024);
	    rest /= 102;
	    return k + "," + rest + "КБ";
	}
	return "" + num + "Б";
    }



}
