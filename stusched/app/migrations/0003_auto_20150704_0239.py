# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0002_auto_20150704_0229'),
    ]

    operations = [
        migrations.RenameField(
            model_name='student',
            old_name='parent',
            new_name='parents',
        ),
        migrations.RemoveField(
            model_name='student',
            name='section',
        ),
        migrations.AddField(
            model_name='student',
            name='sections',
            field=models.ManyToManyField(blank=True, to='app.Section'),
        ),
    ]
