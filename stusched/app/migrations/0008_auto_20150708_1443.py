# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0007_auto_20150708_1222'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='student',
            name='parents',
        ),
        migrations.AddField(
            model_name='student',
            name='parent',
            field=models.ForeignKey(to='app.Parent', default=1),
            preserve_default=False,
        ),
    ]
