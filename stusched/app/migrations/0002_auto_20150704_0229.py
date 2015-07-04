# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0001_initial'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='student',
            name='parent',
        ),
        migrations.AddField(
            model_name='student',
            name='parent',
            field=models.ManyToManyField(to='app.Parent'),
        ),
        migrations.RemoveField(
            model_name='student',
            name='section',
        ),
        migrations.AddField(
            model_name='student',
            name='section',
            field=models.ManyToManyField(to='app.Section'),
        ),
    ]
