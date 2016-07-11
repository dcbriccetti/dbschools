# -*- coding: utf-8 -*-
# Generated by Django 1.9.5 on 2016-07-05 18:12
from __future__ import unicode_literals

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0018_auto_20160630_1858'),
    ]

    operations = [
        migrations.CreateModel(
            name='KnowledgeItem',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=100)),
            ],
        ),
        migrations.CreateModel(
            name='Knows',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('quantity', models.IntegerField()),
                ('item', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='app.KnowledgeItem')),
            ],
        ),
        migrations.AddField(
            model_name='student',
            name='aptitude',
            field=models.IntegerField(null=True),
        ),
        migrations.AddField(
            model_name='student',
            name='wants_courses',
            field=models.ManyToManyField(blank=True, to='app.Course'),
        ),
        migrations.AddField(
            model_name='student',
            name='when_available',
            field=models.TextField(blank=True),
        ),
        migrations.AddField(
            model_name='knows',
            name='student',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='app.Student'),
        ),
        migrations.AddField(
            model_name='knowledgeitem',
            name='students',
            field=models.ManyToManyField(through='app.Knows', to='app.Student'),
        ),
    ]